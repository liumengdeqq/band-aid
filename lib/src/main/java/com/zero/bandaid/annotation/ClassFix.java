package com.zero.bandaid.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chaopei on 2016/7/6.
 * 类替换注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassFix {
    /**
     * 被替换的类
     * @return
     */
    String clazz();
}
