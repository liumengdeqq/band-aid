package com.zero.bandaid;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.zero.bandaid.patch.MethodInfo;
import com.zero.bandaid.patch.MethodPatch;
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
            Runtime.getRuntime().loadLibrary("band-aid");
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "loadLibrary", e);
            }
        }
    }

    private static HotFix sInstance;

    public static synchronized HotFix getInstance() {
        if (null == sInstance) {
            sInstance = new HotFix();
        }
        return sInstance;
    }

//    public synchronized void addPatch(MethodPatch patch) {
////        if (!isSetuped) {
////            setup();
////        }
//        sPatches.add(patch);
//        patch.setStatus(Patch.Status.Loaded);
//        if (!patch.init()) {
//            if (DEBUG) {
//                Log.e(TAG, "[addPatch] : patch init failed, name=" + patch.getPatchName());
//            }
//            return;
//        }
//        patch.setStatus(Patch.Status.Inited);
//        for (String clazz : patch.getSrcClasses()) {
//            for (MethodInfo method : patch.getSrcDstMethods(clazz)) {
//                applyPatch(method.getSrc(), method.getDst(), method.getMode());
//                initFields(method.getDst().getDeclaringClass());
//            }
//        }
//        patch.setStatus(Patch.Status.Fixed);
//    }

    public Context getContext() {
        return mContext;
    }

    /**
     * initialize
     *
     * @return true if initialize success
     */
    public synchronized boolean setup(Context context) {
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
        if (!Patch.setupNative(isArt, apilevel)) {
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
                if (!odexDir.mkdirs()) {
                    return false;
                }
            }
            Patch patch = Patch.createFromFile(patchDexFile.getAbsolutePath(), odexDir.getAbsolutePath() + "/test_patch.odex");
            if (null == patch) {
                if (DEBUG) {
                    Log.e(TAG, "[loadPatches], patch = null");
                }
                return false;
            }
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
                    if (patch.apply()) {
                        patch.setStatus(Patch.Status.Fixed);
                    }
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
                        Log.e(TAG, "[initPatches] : patch init failed, name=" + patch.getName());
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

}
