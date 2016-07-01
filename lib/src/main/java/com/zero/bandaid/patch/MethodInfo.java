package com.zero.bandaid.patch;

import java.lang.reflect.Method;

/**
 * Created by chaopei on 2016/2/27.
 * 包含被替换方法和替换方法
 */
public class MethodInfo {
    /**
     * 要替换的原方法
     */
    private Method mSrc;

    /**
     * 要替换成的目标方法
     */
    private Method mDst;

    /**
     * 替换的模式
     */
    private int mMode;

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
