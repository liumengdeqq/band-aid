//
// Created by chaopei on 2016/5/22.
//

#include "ArtBridge.h"

#include "art/art.h"

const ArtBridge *ArtBridge::sInstance = new ArtBridge;

jboolean ArtBridge::setup(JNIEnv *env, int apilevel) {
//    if (19 >= apilevel) {
//        // kitkat上也支持art
//        return JNI_FALSE;
//    } else {
    mApiLevel = apilevel;
    return art_setup(env, apilevel);
//    }
}

void ArtBridge::setFieldFlag(JNIEnv *env, jobject field) {
    if (mApiLevel > 22) {
        setFieldFlag_6_0(env, field);
    } else if (mApiLevel > 21) {
        setFieldFlag_5_1(env, field);
    } else {
        setFieldFlag_5_0(env, field);
    }
}

void ArtBridge::applyPatch(JNIEnv *env, jobject src, jobject dest, Mode mode) {
    switch (mode) {
        case REPLACE:
            if (mApiLevel > 22) {
                replace_6_0(env, src, dest);
            } else if (mApiLevel > 21) {
                replace_5_1(env, src, dest);
            } else {
                replace_5_0(env, src, dest);
            }
            break;
        case DISPATCH_CPP:
            art_dispatch_6_0(env, src, dest, false);
            break;
        case DISPATCH_JAVA:
            art_dispatch_6_0(env, src, dest, true);
            break;
        default:
            break;
    }
}
