package com.zero.bandaid;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by chaopei on 2016/7/14.
 */
public class AppUtil {

    public static final boolean DEBUG = Env.DEBUG;

    public static final String TAG = "AppUtil";

    private static String sCurProcessName;

    private static String sPackageName;

    private static String readProcessNameFromCmdline() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/self/cmdline"), Charset.defaultCharset()));
            String line = reader.readLine();
            if (null != line) {
                return line.trim();
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "[getCurrentProcessName]: ", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    /**
     * 返回当前的进程名
     */
    public static String getProcessName() {
        if (TextUtils.isEmpty(sCurProcessName)) {
            sCurProcessName = readProcessNameFromCmdline();
        }
        return sCurProcessName;
    }

    public static String getPackageName() {
        if (null == sPackageName) {
            sPackageName = HotFix.getInstance().getContext().getPackageName();
        }
        return sPackageName;
    }

}
