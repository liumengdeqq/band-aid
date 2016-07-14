package com.zero.bandaid.patch;

import android.content.Context;

import com.zero.bandaid.Env;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import dalvik.system.DexFile;

/**
 * Created by chaopei on 2016/2/26.
 * APK文件patch
 */
public class ApkOrJarPatch extends Patch {

    private static final boolean DEBUG = Env.DEBUG;

    private static final String TAG = DEBUG ? "ApkOrJarPatch" : ApkOrJarPatch.class.getSimpleName();

    public static final String ENTRY_NAME = "META-INFO/PATCH.MF";
    public static final String PATCH_NAME = "Patch-Name";
    public static final String PATCH_VERSION_BUILD = "Patch-VersionBuild";
    public static final String PATCH_TIMESTAMP = "Patch-Timestamp";
    public static final String PATCH_CLASSES = "Patch-Classes";

    /**
     * patch 所在的路径
     */
    private String mApkOrJarPath;

    /**
     * dexopt/dex2oat 后的 dex 文件
     */
    private String mODexPath;

    /**
     * 加载的 DexFile
     */
    private DexFile mDex;

    /**
     * @param apkOrJarPath 要加载的apk文件路径
     * @param optDexPath   要存储的 odex 文件路径
     * @throws IOException
     */
    public ApkOrJarPatch(Context context, String apkOrJarPath, String optDexPath) throws IOException {
        super();
        mApkOrJarPath = apkOrJarPath;
        mODexPath = optDexPath;
        mDex = DexFile.loadDex(mApkOrJarPath, mODexPath, 0);
    }

    @Override
    public Info initPatchInfo() {
        JarFile jarFile = null;
        InputStream inputStream = null;
        try {
            // 读取PATCH.MF中的属性
            jarFile = new JarFile(new File(mApkOrJarPath));
            JarEntry entry = jarFile.getJarEntry(ENTRY_NAME);
            inputStream = jarFile.getInputStream(entry);
            Manifest manifest = new Manifest(inputStream);
            Attributes main = manifest.getMainAttributes();
            // patch名
            String name = main.getValue(PATCH_NAME);
            // patch对应主工程版本号
            String versionBuild = main.getValue(PATCH_VERSION_BUILD);
            // patch创建的时间戳
            long timestamp = Long.parseLong(main.getValue(PATCH_TIMESTAMP));
            return new Info(name, timestamp, versionBuild);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public Class<?> loadPatchClass(String patchClass) {
        return mDex.loadClass(patchClass, mClassLoader);
    }

    @Override
    public ClassLoader initClassLoader() {
        return new ClassLoader(getClass().getClassLoader()) {
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
    public String[] initPatchClasses() {
        JarFile jarFile = null;
        InputStream inputStream = null;
        try {
            // 读取PATCH.MF中的属性
            jarFile = new JarFile(new File(mApkOrJarPath));
            JarEntry entry = jarFile.getJarEntry(ENTRY_NAME);
            inputStream = jarFile.getInputStream(entry);
            Manifest manifest = new Manifest(inputStream);
            Attributes main = manifest.getMainAttributes();
            return main.getValue(PATCH_CLASSES).split(",");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isCurrentProcessApply() {
        return true;
    }

}
