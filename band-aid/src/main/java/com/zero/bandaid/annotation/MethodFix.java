package com.zero.bandaid.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chaopei on 2016/2/27.
 * 在patch中用于修饰替换方法的
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodFix {

    public final int MODE_DISPATCH_JAVA = 0, MODE_DISPATCH_CPP = 1, MODE_REPLACE = 2;

    /**
     * 被替换的方法所在类
     * @return
     */
    String clazz();

    /**
     * 被替换的方法名
     * @return
     */
    String method();

    /**
     * 替换模式
     * @return
     */
    int mode();
}
