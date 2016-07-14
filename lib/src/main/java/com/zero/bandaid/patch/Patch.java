package com.zero.bandaid.patch;

import android.text.TextUtils;
import android.util.Log;

import com.zero.bandaid.AppUtil;
import com.zero.bandaid.HotFix;
import com.zero.bandaid.Env;
import com.zero.bandaid.annotation.ClassFix;
import com.zero.bandaid.annotation.MethodFix;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * Created by chaopei on 2016/2/27.
 * <p/>
 * Patch 抽象类，表示一个 patch
 */
public abstract class Patch {

    class Info {
        String name;
        long timestamp;
        String versionBuild;
        String[] srcClasses = null;
        /**
         * all - 所有进程，main - 主进程，以":"开头的词 - 其他指定进程，其他字符 - 与all相同
         */
        String applyProcess;

        Info(String name, long timestamp, String versionBuild, String[] srcClasses, String applyProcess) {
            this.name = name;
            this.timestamp = timestamp;
            this.versionBuild = versionBuild;
            this.srcClasses = srcClasses;
            this.applyProcess = applyProcess;
        }
    }

    private static final boolean DEBUG = Env.DEBUG;

    private static final String TAG = DEBUG ? "Patch" : Patch.class.getSimpleName();

    public enum Status {
        Unloaded, Loaded, Inited, Fixed
    }

    /**
     * 加载的 DexFile
     */
    private DexFile mDex;

    /**
     * 加载它的 ClassLoader
     */
    protected ClassLoader mClassLoader;

    public static final String DEFAULT_PATCH_NAME = "Unknown";

    protected Info mPatchInfo;

    protected Status mStatus = Status.Unloaded;

    /**
     * 被替换方法与替换成的方法，Key是被替换的类
     */
    private Map<String, List<MethodInfo>> mPatchMethods = new HashMap<>();

    /**
     * 被替换类与替换成的类，Key是被替换的类
     */
    private Map<Class<?>, Class<?>> mPatchCLasses = new HashMap<>();

    /**
     * 初始化patch基本信息
     *
     * @return
     */
    public abstract Info initPatchInfo();

    public abstract DexFile initDexFile();

    private boolean isCurrentProcessApply() {
        if (TextUtils.isEmpty(getApplyProcess())) { // all
            return true;
        } else if ("main".equals(getApplyProcess())) { // 主进程
            return AppUtil.getPackageName().equals(AppUtil.getProcessName());
        } else if (getApplyProcess().startsWith(":")) { // 指定进程
            return AppUtil.getProcessName().endsWith(getApplyProcess());
        } else { // all
            return true;
        }
    }

    public Patch() {
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
            // 判断该类是否是替换类
            ClassFix srcClazzAnnotation = dstClazz.getAnnotation(ClassFix.class);
            if (null != srcClazzAnnotation) { // 说明是替换类
                String srcClassStr = srcClazzAnnotation.clazz();
                try {
                    Class<?> srcClazz = Class.forName(srcClassStr);
                    mPatchCLasses.put(srcClazz, dstClazz);
                    return true;
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "", e);
                    return false;
                }
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
                Class<?> srcClazz = HotFix.initTargetClass(Class.forName(clz));
                if (null != srcClazz && !TextUtils.isEmpty(meth)) {
                    if (DEBUG) {
                        Log.d(TAG, "[init] : src clz= " + clz);
                        Log.d(TAG, "[init] : src meth= " + meth);
                    }
                    Method srcMethod = srcClazz.getDeclaredMethod(meth, dstMethod.getParameterTypes());
                    methods.add(new MethodInfo(srcMethod, dstMethod, MethodFix.MODE_DISPATCH_CPP));
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

    public boolean init() {
        mDex = initDexFile();
        mClassLoader = initClassLoader();
        mPatchInfo = initPatchInfo();
        if (!isCurrentProcessApply()) {
            return false;
        }
        try {
            // patch中哪些类是包含被替换方法的
            String[] classStrs = getSrcClasses();
            for (String str : classStrs) {
                String classStr = str.trim();
                if (!TextUtils.isEmpty(classStr)) {
                    if (!initPatchClass(classStr)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //////////////////////////////////Getters/////////////////////////////////////

    public List<MethodInfo> getSrcDstMethods(String srcClass) {
        return mPatchMethods.get(srcClass);
    }

    public Map<String, List<MethodInfo>> getPatchMethods() {
        return mPatchMethods;
    }

    public Map<Class<?>, Class<?>> getPatchClasses() {
        return mPatchCLasses;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
    }

    public String[] getSrcClasses() {
        return mPatchInfo.srcClasses;
    }

    public String getPatchName() {
        return mPatchInfo.name;
    }

    public long getTimestamp() {
        return mPatchInfo.timestamp;
    }

    /**
     * @return 返回该patch对应的版本号+build号，如 6.3.0.1234
     */
    public String getVersionBuild() {
        return mPatchInfo.versionBuild;
    }

    public String getApplyProcess() {
        return mPatchInfo.applyProcess;
    }
}
