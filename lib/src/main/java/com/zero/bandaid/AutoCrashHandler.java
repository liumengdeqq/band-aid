package com.zero.bandaid;

/**
 * Created by chaopei on 2016/7/7.
 */
public class AutoCrashHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //todo 弹窗提示，并尝试自修复
    }

}
