package com.zero.bandaid.patch;

import android.text.TextUtils;
import android.util.Log;

import com.zero.bandaid.Env;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Created by chaopei on 2016/7/22.
 */
public abstract class Patch {

    private static final boolean DEBUG = Env.DEBUG;

    private static final String TAG = DEBUG ? "Patch" : Patch.class.getSimpleName();

    public static final String ENTRY_NAME = "META-INFO/PATCH.MF";
    public static final String PATCH_NAME = "Patch-Name";
    public static final String PATCH_MODE = "Patch-Mode";
    public static final String PATCH_VERSION_BUILD = "Patch-VersionBuild";
    public static final String PATCH_TIMESTAMP = "Patch-Timestamp";
    public static final String PATCH_CLASSES = "Patch-Classes";
    public static final String PATCH_APPLY_PROCESS = "Patch-ApplyProcess";

    public enum Status {
        Unloaded, Loaded, Inited, Fixed
    }

    enum Mode {
        Method, Class
    }

    static class Info {
        /**
         * INFO中读出的字符串应是 class 或者 method
         */
        Mode mode;
        String name;
        long timestamp;
        String versionBuild;
        String[] srcClasses = null;
        /**
         * patch 所在的路径
         */
        String path;

        /**
         * dexopt/dex2oat 后的 dex 文件
         */
        String odexPath;
        /**
         * all - 所有进程，main - 主进程，以":"开头的词 - 其他指定进程，其他字符 - 与all相同
         */
        String applyProcess;

        Info(String path, String odexPath, String name, Mode mode, long timestamp, String versionBuild, String applyProcess, String[] srcClasses) {
            this.path = path;
            this.odexPath = odexPath;
            this.name = name;
            this.mode = mode;
            this.timestamp = timestamp;
            this.versionBuild = versionBuild;
            this.srcClasses = srcClasses;
            this.applyProcess = applyProcess;
        }
    }

    private Status mStatus = Status.Unloaded;

    private Info mInfo;

    public abstract boolean init();

    public abstract boolean apply();

    Patch(Info info) {
        mInfo = info;
    }

    public Info getInfo() {
        return mInfo;
    }

    public String getName() {
        return mInfo.name;
    }

    public String getPath() {
        return mInfo.path;
    }

    public String getOdexPath() {
        return mInfo.odexPath;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }

    /**
     * 初始化 HotFix
     */
    public static native boolean setupNative(boolean isArt, int apilevel);

    public static native void applyMethodPatchNative(Method src, Method dst, int mode);

    public static native void setFieldFlagNative(Field field);

    public static Patch createFromFile(String path, String odexPath) {
        Info patchInfo = createInfoFromFile(path, odexPath);
        if (null == patchInfo) {
            return null;
        }
        Patch patch = null;
        if (Mode.Method == patchInfo.mode) {
            patch = new MethodPatch(patchInfo);
        } else if (Mode.Class == patchInfo.mode) {
            patch = new ClassPatch(patchInfo);
        }
        return patch;
    }

    public static Info createInfoFromFile(String path, String odexPath) {
        JarFile jarFile = null;
        InputStream inputStream = null;
        try {
            // 读取PATCH.MF中的属性
            jarFile = new JarFile(new File(path));
            JarEntry entry = jarFile.getJarEntry(ENTRY_NAME);
            if (null == entry) {
                return null;
            }
            inputStream = jarFile.getInputStream(entry);
            Manifest manifest = new Manifest(inputStream);
            Attributes main = manifest.getMainAttributes();
            // patch名
            String name = main.getValue(PATCH_NAME);
            if (TextUtils.isEmpty(name)) {
                if (DEBUG) {
                    Log.e(TAG, "error: patch name is empty");
                }
                return null;
            }
            String modeStr = main.getValue(PATCH_MODE);
            Mode mode;
            if (modeStr.equalsIgnoreCase("class")) {
                mode = Mode.Class;
            } else if (modeStr.equalsIgnoreCase("method")) {
                mode = Mode.Method;
            } else {
                if (DEBUG) {
                    Log.e(TAG, "error: patch mode format error, mode=" + modeStr);
                }
                return null;
            }
            // patch对应主工程版本号
            String versionBuild = main.getValue(PATCH_VERSION_BUILD);
            if (TextUtils.isEmpty(versionBuild)) {
                if (DEBUG) {
                    Log.e(TAG, "error: versionBuild is empty");
                }
                return null;
            }
            // patch创建的时间戳
            long timestamp = Long.parseLong(main.getValue(PATCH_TIMESTAMP));
            if (0 >= timestamp) {
                if (DEBUG) {
                    Log.e(TAG, "error: timestamp must be positive");
                }
                return null;
            }
            String applyProcess = main.getValue(PATCH_APPLY_PROCESS);

            String[] classes = null;
            if (Mode.Method == mode) {
                String classesStr = main.getValue(PATCH_CLASSES);
                if (TextUtils.isEmpty(classesStr)) {
                    if (DEBUG) {
                        Log.e(TAG, "error: classes is empty");
                    }
                    return null;
                }
                classes = classesStr.split(",");
            }
            return new Info(path, odexPath, name, mode, timestamp, versionBuild, applyProcess, classes);
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
}
