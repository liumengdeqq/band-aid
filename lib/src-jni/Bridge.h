//
// Created by chaopei on 2016/5/22.
//

#ifndef HOTPATCH_BRIDGE_H
#define HOTPATCH_BRIDGE_H

#include "common.h"

enum Mode {
    DISPATCH_JAVA, DISPATCH_CPP, REPLACE
};

class Bridge {

public:

    virtual jboolean setup(JNIEnv *env, int apilevel) = 0;

    virtual void applyPatch(JNIEnv *env, jobject src, jobject dest, Mode mode) = 0;

    virtual void setFieldFlag(JNIEnv *env, jobject field) = 0;
};


#endif //HOTPATCH_BRIDGE_H
