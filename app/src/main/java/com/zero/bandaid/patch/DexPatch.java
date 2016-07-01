package com.zero.bandaid.patch;

import android.content.Context;
import android.util.Log;

import com.zero.bandaid.Env;
import java.io.IOException;
import dalvik.system.DexFile;

/**
 * Created by chaopei on 2016/2/27.
 * Dex文件patch，just for test by now.
 */
public class DexPatch extends Patch {


    private static final boolean DEBUG = Env.DEBUG;

    private static final String TAG = DEBUG ? "DexPatch" : DexPatch.class.getSimpleName();


    /**
     * patch 所在的路径
     */
    private String mDexPath;

    /**
     * dexopt/dex2oat 后的 dex 文件
     */
    private String mODexPath;

    /**
     * 加载的 DexFile
     */
    private DexFile mDex;

    /**
     * @param dexPath    要加载的apk文件路径
     * @param optDexPath 要存储的 odex 文件路径
     * @throws IOException
     */
    public DexPatch(Context context, String dexPath, String optDexPath) throws IOException {
        super(context);
        mDexPath = dexPath;
        mODexPath = optDexPath;
        mDex = DexFile.loadDex(mDexPath, mODexPath, 0);
    }

    public ClassLoader initClassLoader() {
        return new ClassLoader(getClass().getClassLoader()){//这个classloader设置!!!!!
            @Override
            protected Class<?> findClass(String className) throws ClassNotFoundException {
                Class<?> clazz = mDex.loadClass(className, mClassLoader);
                if (null == clazz) {
                    throw new ClassNotFoundException(className);
                }
                return clazz;
            }
        };
    }

    public Class<?> loadPatchClass(String patchClass) {
        Class<?> clazz = null;
        try {
            if (DEBUG) {
                Log.e(TAG, "[loadPatchClass] : patchClass="+patchClass);
            }
            clazz = Class.forName(patchClass, true, mClassLoader);
            if (DEBUG) {
                Log.e(TAG, "[loadPatchClass] : clazz="+clazz);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG,"", e);
        }
        return clazz;
//        return mDex.loadClass(patchClass, mClassLoader);
    }

    public void initMetaInfo() {
        //none
    }

    public String[] initPatchClasses() {
        // 暂时写死
        return new String[]{"com.zero.Test"};
    }

    @Override
    public boolean isCurrentProcessApply() {
        return true;
    }
}
