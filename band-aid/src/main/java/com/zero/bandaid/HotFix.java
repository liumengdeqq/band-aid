package com.zero.bandaid;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.zero.bandaid.patch.DexPatch;
import com.zero.bandaid.patch.MethodInfo;
import com.zero.bandaid.patch.Patch;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chaopei on 2016/2/25.
 * HotFix 工具类
 */
public class HotFix {

    private static final boolean DEBUG = Env.DEBUG;

    private static final String TAG = DEBUG ? "HotFix" : HotFix.class.getSimpleName();

    private static List<Patch> sPatches = new ArrayList<Patch>();

    private static boolean isSetuped;

    private Context mContext;

    static {
        try {
            Runtime.getRuntime().loadLibrary("hotpatch");
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "loadLibrary", e);
            }
        }
    }
    private static HotFix sInstance;

    public synchronized static HotFix getInstance() {
        if (null == sInstance) {
            sInstance = new HotFix();
        }
        return sInstance;
    }

    synchronized public void addPatch(Patch patch) {
//        if (!isSetuped) {
//            setup();
//        }
        sPatches.add(patch);
        patch.setStatus(Patch.Status.Loaded);
        if (!patch.init()) {
            if (DEBUG) {
                Log.e(TAG, "[addPatch] : patch init failed, name=" + patch.getPatchName());
            }
            return;
        }
        patch.setStatus(Patch.Status.Inited);
        for (String clazz : patch.getPatchClasses()) {
            for (MethodInfo method : patch.getSrcDstMethods(clazz)) {
                applyPatch(method.getSrc(), method.getDst(), method.getMode());
                initFields(method.getDst().getDeclaringClass());
            }
        }
        patch.setStatus(Patch.Status.Fixed);
    }

    /**
     * initialize
     *
     * @return true if initialize success
     */
    synchronized public boolean setup(Context context) {
        if (isSetuped) {
            return true;
        }
        mContext = context;
        final String vmVersion = System.getProperty("java.vm.version");
        boolean isArt = vmVersion != null && vmVersion.startsWith("2");
        int apilevel = Build.VERSION.SDK_INT;
        if (DEBUG) {
            Log.e(TAG, "[setup] : vmVersion = " + vmVersion);
            Log.e(TAG, "[setup] : isArt = " + isArt);
            Log.e(TAG, "[setup] : apilevel = " + apilevel);
        }
        // HotFix 相关初始化
        if (!setupNative(isArt, apilevel)) {
            if (DEBUG) {
                Log.e(TAG, "[setup] :setupNative failed");
            }
            return false;
        }
        // 加载所有patch进内存
        if (!loadPatches()) {
            if (DEBUG) {
                Log.e(TAG, "[setup] : loadPatches failed");
            }
            return false;
        }
        // 初始化所有patch中的类、方法
        if (!initPatches()) {
            if (DEBUG) {
                Log.e(TAG, "[setup] : initPatches failed");
            }
            return false;
        }
        // 应用每个patch的修改
        if (!applyAll()) {
            if (DEBUG) {
                Log.e(TAG, "[setup] : applyAll failed");
            }
            return false;
        }
        isSetuped = true;
        return isSetuped;
    }

    /**
     * 加载所有patch进内存
     */
    private boolean loadPatches() {
//        File patchDexFile = new File(mContext.getFilesDir().getAbsolutePath() + "/patches", "test_patch.dex");
//        File patchDexFile = new File(mContext.getFilesDir().getAbsolutePath() + "/patches", IPC.isUIProcess()?"test_patch.dex":"test_patch1.dex");
//        if (patchDexFile.exists()) {
//            patchDexFile.delete();
//        }
//        AssetsUtils.quickExtractTo(mContext, "patches/test_patch.dex", patchDexFile.getParent(), "test_patch.dex", true, null, null);

        File patchDexFile = new File(mContext.getFilesDir().getAbsolutePath() + "test_patch.dex");
        try {
            File odexDir = new File(patchDexFile.getParentFile().getAbsolutePath() + "/opatches/");
            if (!odexDir.isDirectory()) {
                odexDir.mkdirs();
            }
            Patch patch = new DexPatch(mContext, patchDexFile.getAbsolutePath(), odexDir.getAbsolutePath() + "/test_patch.odex");
            sPatches.add(patch);
            patch.setStatus(Patch.Status.Loaded);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "[loadPatches]", e);
            }
            return false;
        }
    }

    /**
     * 应用每个patch的修改
     */
    private boolean applyAll() {
        try {
            for (Patch patch : sPatches) {
                if (Patch.Status.Inited == patch.getStatus()) {
                    for (Map.Entry<String, List<MethodInfo>> entry : patch.getPatchMethods().entrySet()) {
                        for (MethodInfo method : entry.getValue()) {
                            applyPatch(method.getSrc(), method.getDst(), method.getMode());
                            initFields(method.getDst().getDeclaringClass());
                        }
                    }
                    patch.setStatus(Patch.Status.Fixed);
                }
            }
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "[applyAll]", e);
            }
            return false;
        }
    }

    /**
     * 初始化所有patch中的类、方法
     */
    private boolean initPatches() {
        try {
            for (Patch patch : sPatches) {
                if (!patch.init()) {
                    if (DEBUG) {
                        Log.e(TAG, "[initPatches] : patch init failed, name=" + patch.getPatchName());
                    }
                    continue;
                }
                patch.setStatus(Patch.Status.Inited);
            }
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "[initPatches]", e);
            }
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
    public static Class<?> initTargetClass(Class<?> clazz) {
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
            setFieldFlagNative(srcField);
        }
    }

    private static void applyPatch(Method src, Method dst, int mode){
        if (DEBUG) {
            Log.d(TAG, "[applyPatch] ; src = " + src.getDeclaringClass().getName() + "." + src.getName());
            Log.d(TAG, "[applyPatch] ; dst = " + dst.getDeclaringClass().getName() + "." + dst.getName());
        }
        applyPatchNative(src, dst, mode);
    }

    /**
     * 初始化 HotFix
     */
    private static native boolean setupNative(boolean isArt, int apilevel);

    private static native void applyPatchNative(Method src, Method dst, int mode);

    private static native void setFieldFlagNative(Field field);
}
