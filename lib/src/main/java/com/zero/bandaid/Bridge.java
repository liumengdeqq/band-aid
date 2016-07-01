package com.zero.bandaid;

import android.util.Log;

import java.lang.reflect.Member;

/**
 * Created by chaopei on 2016/5/24.
 * java 中转
 */
public class Bridge {

    public static Object handleHookedMethod(Member method, int originalMethodId, Object dstMeth,
                                            Object thisObject, Object[] args) {
        Log.e("Bridge", "handleHookedMethod");
        if (null != args) {
            for (Object arg : args) {
                Log.e("Bridge", "arg=" + arg);
            }
        }
        return 0;
    }

}
