package com.zero.bandaid.annotation;

/**
 * Created by chaopei on 2016/7/6.
 * 类替换注解
 */
public @interface ClassFix {
    /**
     * 被替换的类
     * @return
     */
    String clazz();
}
