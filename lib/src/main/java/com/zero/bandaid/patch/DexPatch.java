package com.zero.bandaid.patch;

import android.content.Context;
import android.util.Log;

import com.zero.bandaid.Env;

import java.io.IOException;
import java.util.Date;

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
     * @param dexPath    要加载的apk文件路径
     * @param optDexPath 要存储的 odex 文件路径
     * @throws IOException
     */
    public DexPatch(Context context, String dexPath, String optDexPath) {
        super();
        mDexPath = dexPath;
        mODexPath = optDexPath;
    }

    @Override
    public DexFile initDexFile() {
        try {
            return DexFile.loadDex(mDexPath, mODexPath, 0);
        } catch (IOException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public Info initPatchInfo() {
        //none
        return new Info(Patch.DEFAULT_PATCH_NAME, new Date().getTime()/1000, "1.0.0.1000", new String[]{"com.zero.Test"}, "all");
    }

}
