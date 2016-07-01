package com.zero.bandaid.patch;

import java.lang.reflect.Method;

/**
 * Created by chaopei on 2016/2/27.
 * 包含被替换方法和替换方法
 */
public class MethodInfo {
    Method mSrc;
    Method mDst;
    int mMode;

    public MethodInfo(Method src, Method dst, int mode) {
        mSrc = src;
        mDst = dst;
        mMode = mode;
    }

    public Method getSrc() {
        return mSrc;
    }

    public Method getDst() {
        return mDst;
    }

    public int getMode() {
        return mMode;
    }
}
