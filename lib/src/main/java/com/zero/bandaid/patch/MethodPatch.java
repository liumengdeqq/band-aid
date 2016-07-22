package com.zero.bandaid.patch;

import android.text.TextUtils;
import android.util.Log;

import com.zero.bandaid.AppUtil;
import com.zero.bandaid.HotFix;
import com.zero.bandaid.Env;
import com.zero.bandaid.annotation.ClassFix;
import com.zero.bandaid.annotation.MethodFix;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * Created by chaopei on 2016/2/27.
 * 方法替换的 patch
 */
public class MethodPatch extends Patch {

    private static final boolean DEBUG = Env.DEBUG;

    private static final String TAG = DEBUG ? "MethodPatch" : MethodPatch.class.getSimpleName();

    /**
     * 加载的 DexFile
     */
    private DexFile mDex;

    /**
     * 加载它的 ClassLoader
     */
    private ClassLoader mClassLoader;

    /**
     * 被替换方法与替换成的方法，Key是被替换的类
     */
    private Map<String, List<MethodInfo>> mPatchMethods = new HashMap<>();

    private boolean isCurrentProcessApply() {
        String process = getInfo().applyProcess;
        if (TextUtils.isEmpty(process)) { // all
            return true;
        } else if ("main".equals(process)) { // 主进程
            return AppUtil.getPackageName().equals(AppUtil.getProcessName());
        } else if (process.startsWith(":")) { // 指定进程
            return AppUtil.getProcessName().endsWith(process);
        } else { // all
            return true;
        }
    }

    MethodPatch(Patch.Info info) {
        super(info);
    }

    private Class<?> loadPatchClass(String patchClass) {
        Class<?> clazz = null;
        try {
            if (DEBUG) {
                Log.e(TAG, "[loadPatchClass] : patchClass=" + patchClass);
            }
            clazz = Class.forName(patchClass, true, mClassLoader);
            if (DEBUG) {
                Log.e(TAG, "[loadPatchClass] : clazz=" + clazz);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "", e);
        }
        return clazz;
    }

    /**
     * 解析出包含patch目标方法的类
     *
     * @return
     */
    private boolean initPatchClass(String patchClass) {
        try {
            // 加载该patch class但没有初始化，我们默认写入配置的class一定存在，不存在一定是bug
            Class<?> dstClazz = loadPatchClass(patchClass);
            if (null == dstClazz) {
                return false;
            }
            // 说明是替换方法
            Method[] dstClazzAllMethods = dstClazz.getDeclaredMethods();
            // 标注中包含被替换方法的信息
            MethodFix srcMethodFixAnnotaion;
            for (Method dstMethod : dstClazzAllMethods) {
                // 找到替换方法
                srcMethodFixAnnotaion = dstMethod.getAnnotation(MethodFix.class);
                if (srcMethodFixAnnotaion == null) {
                    continue;
                }
                // 标注中的被修改的类
                String clz = srcMethodFixAnnotaion.clazz();
                List<MethodInfo> methods = mPatchMethods.get(clz);
                if (null == methods) {
                    methods = new ArrayList<>();
                    mPatchMethods.put(clz, methods);
                }
                // 标注中的被修改的方法
                String meth = srcMethodFixAnnotaion.method();
                int mode = srcMethodFixAnnotaion.mode();
                Class<?> srcClazz = initTargetClass(Class.forName(clz));
                if (null != srcClazz && !TextUtils.isEmpty(meth)) {
                    if (DEBUG) {
                        Log.d(TAG, "[init] : src clz= " + clz);
                        Log.d(TAG, "[init] : src meth= " + meth);
                    }
                    Method srcMethod = srcClazz.getDeclaredMethod(meth, dstMethod.getParameterTypes());
                    methods.add(new MethodInfo(srcMethod, dstMethod, mode));
                } else {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * initialize the target class, and modify access flag of class’ fields to
     * public
     *
     * @param clazz target class
     * @return initialized class
     */
    private static Class<?> initTargetClass(Class<?> clazz) {
        try {
            Class<?> targetClazz = Class.forName(clazz.getName(), true,
                    clazz.getClassLoader());

            initFields(targetClazz);
            return targetClazz;
        } catch (Exception e) {
            Log.e(TAG, "initTargetClass", e);
        }
        return null;
    }

    private static void applyPatch(Method src, Method dst, int mode) {
        if (DEBUG) {
            Log.d(TAG, "[applyPatch] ; src = " + src.getDeclaringClass().getName() + "." + src.getName());
            Log.d(TAG, "[applyPatch] ; dst = " + dst.getDeclaringClass().getName() + "." + dst.getName());
        }
        Patch.applyMethodPatchNative(src, dst, mode);
    }

    /**
     * modify access flag of class’ fields to public
     *
     * @param clazz class
     */
    private static void initFields(Class<?> clazz) {
        if (DEBUG) {
            Log.d(TAG, "[initFields]");
        }
        Field[] srcFields = clazz.getDeclaredFields();
        for (Field srcField : srcFields) {
            Log.d(TAG, "[initFields] : modify " + clazz.getName() + "." + srcField.getName()
                    + " flag:");
            Patch.setFieldFlagNative(srcField);
        }
    }

    private ClassLoader initClassLoader() {
        return new ClassLoader(getClass().getClassLoader()) { //这个classloader设置!!!!!
            @Override
            protected Class<?> findClass(String className) throws ClassNotFoundException {
                Class<?> clazz = mDex.loadClass(className, this);
                if (null == clazz) {
                    throw new ClassNotFoundException(className);
                }
                return clazz;
            }
        };
    }

    @Override
    public boolean init() {
        try {
            mDex = DexFile.loadDex(getInfo().path, getInfo().odexPath, 0);
            mClassLoader = initClassLoader();
            if (!isCurrentProcessApply()) {
                return false;
            }
            // patch中哪些类是包含被替换方法的
            String[] classStrs = getInfo().srcClasses;
            for (String str : classStrs) {
                String classStr = str.trim();
                if (!TextUtils.isEmpty(classStr)) {
                    if (!initPatchClass(classStr)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean apply() {
        for (Map.Entry<String, List<MethodInfo>> entry : mPatchMethods.entrySet()) {
            for (MethodInfo method : entry.getValue()) {
                applyPatch(method.getSrc(), method.getDst(), method.getMode());
                initFields(method.getDst().getDeclaringClass());
            }
        }
        return true;
    }

}
