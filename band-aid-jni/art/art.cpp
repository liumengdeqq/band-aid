#include "art_5_0.h"
#include "art_5_1.h"
#include "art_6_0.h"

#include "art.h"

#include "../common.h"

artDeliverPendingExceptionFromCode_func artDeliverPendingExceptionFromCode_fnPtr;

QuickArgumentVisitor_constr QuickArgumentVisitor_constrPtr;

static void *art_dlsym(void *hand, const char *name) {
    void *ret = dlsym(hand, name);
    char msg[1024] = {0};
    snprintf(msg, sizeof(msg) - 1, "0x%x", ret);
#ifdef DEBUG
    LOGD("%s = %s\n", name, msg);
#endif
    return ret;
}

void throwNPE(JNIEnv *env, const char *msg) {
    LOGE("setup error: %s", msg);
//	env->ThrowNew(NPEClazz, msg);
}

jboolean __attribute__ ((visibility ("hidden"))) art_setup(
        JNIEnv *env, int apilevel) {

    void *art_hand = dlopen("libart.so", RTLD_NOW);
    if (art_hand) {
        artDeliverPendingExceptionFromCode_fnPtr = (artDeliverPendingExceptionFromCode_func) art_dlsym(art_hand,
                                                                                   "artDeliverPendingExceptionFromCode");
        if (!artDeliverPendingExceptionFromCode_fnPtr) {
            throwNPE(env, "artDeliverPendingExceptionFromCode_fnPtr");
            return JNI_FALSE;
        }
//        QuickArgumentVisitor_constrPtr = (QuickArgumentVisitor_constr) art_dlsym(art_hand,
//                                                                                 "_ZN3art20QuickArgumentVisitorC2EPNS_14StackReferenceINS_6mirror9ArtMethodEEEbPKcj");
//        if (!QuickArgumentVisitor_constrPtr) {
//            throwNPE(env, "QuickArgumentVisitor_constrPtr");
//            return JNI_FALSE;
//        }
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

extern "C" void artDeliverPendingExceptionFromCode(void *self){
    LOGD("artDeliverPendingExceptionFromCode");
    artDeliverPendingExceptionFromCode_fnPtr(self);
}