//
// Created by chaopei on 2016/5/22.
//

#include "DalvikBridge.h"
#include "dalvik/dalvik_core.h"
#include <assert.h>

const DalvikBridge* DalvikBridge::sInstance = new DalvikBridge;

bool DalvikBridge::setup(JNIEnv *env, int apilevel) {
    int res = dalvik_setup(env, apilevel);
    setStatus(res);
    return 0 == res;
}

void DalvikBridge::setFieldFlag(JNIEnv *env, jobject field) {
    Field *dalvikField = (Field *) env->FromReflectedField(field);
    dalvikField->accessFlags = dalvikField->accessFlags & (~ACC_PRIVATE)
                               | ACC_PUBLIC;
}

void DalvikBridge::applyPatch(JNIEnv *env, jobject src, jobject dest, Mode mode) {
    switch (mode) {
        case DISPATCH_CPP:
            if (!dalvik_is_dispatched(env, src)) {
                dalvik_dispatch(env, src, dest, false);
            }
            break;
        case DISPATCH_JAVA:
            if (!dalvik_is_dispatched(env, src)) {
                dalvik_dispatch(env, src, dest, true);
            }
            break;
        case REPLACE:
            dalvik_replace(env, src, dest);
            break;
        default:
            break;
    }
}
