#include <time.h>
#include <stdlib.h>
#include <assert.h>

#include <stdbool.h>
#include <dlfcn.h>


#include "dalvik_core.h"

dvmComputeMethodArgsSize_func dvmComputeMethodArgsSize_fnPtr;

//dvmIsStaticMethod_func dvmIsStaticMethod_fnPtr;

dvmCallMethod_func dvmCallMethod_fnPtr;

dexProtoGetParameterCount_func dexProtoGetParameterCount_fnPtr;

dvmAllocArrayByClass_func dvmAllocArrayByClass_fnPtr;

dvmBoxPrimitive_func dvmBoxPrimitive_fnPtr;

dvmFindPrimitiveClass_func dvmFindPrimitiveClass_fnPtr;

dvmReleaseTrackedAlloc_func dvmReleaseTrackedAlloc_fnPtr;

dvmFindArrayClass_func dvmFindArrayClass_fnPtr;

//dvmGetArgLong_func dvmGetArgLong_fnPtr;

dvmCheckException_func dvmCheckException_fnPtr;

dvmGetException_func dvmGetException_fnPtr;

dvmCreateReflectMethodObject_func dvmCreateReflectMethodObject_fnPtr;

dvmGetBoxedReturnType_func dvmGetBoxedReturnType_fnPtr;

//dvmIsPrimitiveClass_func dvmIsPrimitiveClass_fnPtr;

dvmUnboxPrimitive_func dvmUnboxPrimitive_fnPtr;

dvmDecodeIndirectRef_func dvmDecodeIndirectRef_fnPtr;

dvmThreadSelf_func dvmThreadSelf_fnPtr;

JNIEnv *jni_env;

ClassObject *classJavaLangObjectArray;
/**
 * NullPointerException
 */
jclass NPEClazz;
/**
 * ClassCastException
 */
jclass CastEClazz;
/**
 * Method.invoke(Object receiver, Object... args)
 */
jmethodID jInvokeMethod;
/**
 * Method.getDeclaringClass()
 */
jmethodID jClassMethod;

static void *dvm_dlsym(void *hand, const char *name);

static void throwNPE(JNIEnv *env, const char *msg);

static s8 dvmGetArgLong(const u4 *args, int elem);

jclass bridgeHandleClass;

Method* bridgeHandleMethod;

jboolean __attribute__ ((visibility ("hidden"))) dalvik_setup(
        JNIEnv *env, int apilevel) {
    jni_env = env;
    void *dvm_hand = dlopen("libdvm.so", RTLD_NOW);
    if (dvm_hand) {
        dvmComputeMethodArgsSize_fnPtr = (dvmComputeMethodArgsSize_func) dvm_dlsym(dvm_hand,
                                                                                   apilevel > 10 ?
                                                                                   "_Z24dvmComputeMethodArgsSizePK6Method"
                                                                                                 :
                                                                                   "dvmComputeMethodArgsSize");
        if (!dvmComputeMethodArgsSize_fnPtr) {
            throwNPE(env, "dvmComputeMethodArgsSize_fnPtr");
            return JNI_FALSE;
        }
        dvmCallMethod_fnPtr = (dvmCallMethod_func) dvm_dlsym(dvm_hand,
                                                             apilevel > 10 ?
                                                             "_Z13dvmCallMethodP6ThreadPK6MethodP6ObjectP6JValuez" :
                                                             "dvmCallMethod");
        if (!dvmCallMethod_fnPtr) {
            throwNPE(env, "dvmCallMethod_fnPtr");
            return JNI_FALSE;
        }
        dexProtoGetParameterCount_fnPtr = (dexProtoGetParameterCount_func) dvm_dlsym(dvm_hand,
                                                                                     apilevel > 10 ?
                                                                                     "_Z25dexProtoGetParameterCountPK8DexProto"
                                                                                                   :
                                                                                     "dexProtoGetParameterCount");
        if (!dexProtoGetParameterCount_fnPtr) {
            throwNPE(env, "dexProtoGetParameterCount_fnPtr");
            return JNI_FALSE;
        }

        dvmAllocArrayByClass_fnPtr = (dvmAllocArrayByClass_func) dvm_dlsym(dvm_hand,
                                                                           "dvmAllocArrayByClass");
        if (!dvmAllocArrayByClass_fnPtr) {
            throwNPE(env, "dvmAllocArrayByClass_fnPtr");
            return JNI_FALSE;
        }
        dvmBoxPrimitive_fnPtr = (dvmBoxPrimitive_func) dvm_dlsym(dvm_hand,
                                                                 apilevel > 10 ?
                                                                 "_Z15dvmBoxPrimitive6JValueP11ClassObject" :
                                                                 "dvmWrapPrimitive");
        if (!dvmBoxPrimitive_fnPtr) {
            throwNPE(env, "dvmBoxPrimitive_fnPtr");
            return JNI_FALSE;
        }
        dvmFindPrimitiveClass_fnPtr = (dvmFindPrimitiveClass_func) dvm_dlsym(dvm_hand,
                                                                             apilevel > 10 ?
                                                                             "_Z21dvmFindPrimitiveClassc"
                                                                                           : "dvmFindPrimitiveClass");
        if (!dvmFindPrimitiveClass_fnPtr) {
            throwNPE(env, "dvmFindPrimitiveClass_fnPtr");
            return JNI_FALSE;
        }
        dvmReleaseTrackedAlloc_fnPtr = (dvmReleaseTrackedAlloc_func) dvm_dlsym(dvm_hand,
                                                                               "dvmReleaseTrackedAlloc");
        if (!dvmReleaseTrackedAlloc_fnPtr) {
            throwNPE(env, "dvmReleaseTrackedAlloc_fnPtr");
            return JNI_FALSE;
        }
        dvmCheckException_fnPtr = (dvmCheckException_func) dvm_dlsym(dvm_hand,
                                                                     apilevel > 10 ?
                                                                     "_Z17dvmCheckExceptionP6Thread"
                                                                                   : "dvmCheckException");
        if (!dvmCheckException_fnPtr) {
            throwNPE(env, "dvmCheckException_fnPtr");
            return JNI_FALSE;
        }

        dvmGetException_fnPtr = (dvmGetException_func) dvm_dlsym(dvm_hand,
                                                                 apilevel > 10 ?
                                                                 "_Z15dvmGetExceptionP6Thread" : "dvmGetException");
        if (!dvmGetException_fnPtr) {
            throwNPE(env, "dvmGetException_fnPtr");
            return JNI_FALSE;
        }
        dvmFindArrayClass_fnPtr = (dvmFindArrayClass_func) dvm_dlsym(dvm_hand,
                                                                     apilevel > 10 ?
                                                                     "_Z17dvmFindArrayClassPKcP6Object" :
                                                                     "dvmFindArrayClass");
        if (!dvmFindArrayClass_fnPtr) {
            throwNPE(env, "dvmFindArrayClass_fnPtr");
            return JNI_FALSE;
        }
        dvmCreateReflectMethodObject_fnPtr = (dvmCreateReflectMethodObject_func) dvm_dlsym(dvm_hand,
                                                                                           apilevel > 10 ?
                                                                                           "_Z28dvmCreateReflectMethodObjectPK6Method"
                                                                                                         :
                                                                                           "dvmCreateReflectMethodObject");
        if (!dvmCreateReflectMethodObject_fnPtr) {
            throwNPE(env, "dvmCreateReflectMethodObject_fnPtr");
            return JNI_FALSE;
        }

        dvmGetBoxedReturnType_fnPtr = (dvmGetBoxedReturnType_func) dvm_dlsym(dvm_hand,
                                                                             apilevel > 10 ?
                                                                             "_Z21dvmGetBoxedReturnTypePK6Method" :
                                                                             "dvmGetBoxedReturnType");
        if (!dvmGetBoxedReturnType_fnPtr) {
            throwNPE(env, "dvmGetBoxedReturnType_fnPtr");
            return JNI_FALSE;
        }
        dvmUnboxPrimitive_fnPtr = (dvmUnboxPrimitive_func) dvm_dlsym(dvm_hand,
                                                                     apilevel > 10 ?
                                                                     "_Z17dvmUnboxPrimitiveP6ObjectP11ClassObjectP6JValue"
                                                                                   :
                                                                     "dvmUnwrapPrimitive");
        if (!dvmUnboxPrimitive_fnPtr) {
            throwNPE(env, "dvmUnboxPrimitive_fnPtr");
            return JNI_FALSE;
        }
        dvmDecodeIndirectRef_fnPtr = (dvmDecodeIndirectRef_func) dvm_dlsym(dvm_hand,
                                                                           apilevel > 10 ?
                                                                           "_Z20dvmDecodeIndirectRefP6ThreadP8_jobject"
                                                                                         :
                                                                           "dvmDecodeIndirectRef");
        if (!dvmDecodeIndirectRef_fnPtr) {
            throwNPE(env, "dvmDecodeIndirectRef_fnPtr");
            return JNI_FALSE;
        }
        dvmThreadSelf_fnPtr = (dvmThreadSelf_func) dvm_dlsym(dvm_hand,
                                                             apilevel > 10 ? "_Z13dvmThreadSelfv" : "dvmThreadSelf");
        if (!dvmThreadSelf_fnPtr) {
            throwNPE(env, "dvmThreadSelf_fnPtr");
            return JNI_FALSE;
        }

        classJavaLangObjectArray = dvmFindArrayClass_fnPtr(
                "[Ljava/lang/Object;", NULL);
        jclass clazz = env->FindClass("java/lang/reflect/Method");
        jInvokeMethod = env->GetMethodID(clazz, "invoke",
                                         "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        jClassMethod = env->GetMethodID(clazz, "getDeclaringClass",
                                        "()Ljava/lang/Class;");
        NPEClazz = env->FindClass("java/lang/NullPointerException");
        CastEClazz = env->FindClass("java/lang/ClassCastException");

        bridgeHandleClass = env->FindClass("com/qihoo360/hotpatch/Bridge");
        bridgeHandleClass = reinterpret_cast<jclass>(env->NewGlobalRef(bridgeHandleClass));

        bridgeHandleMethod = (Method*) env->GetStaticMethodID(bridgeHandleClass, "handleHookedMethod",
                                                                      "(Ljava/lang/reflect/Member;ILjava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

void throwNPE(JNIEnv *env, const char *msg) {
    LOGE("setup error: %s", msg);
//	env->ThrowNew(NPEClazz, msg);
}

bool dvmIsStaticMethod(const Method *method) {
    return (method->accessFlags & ACC_STATIC) != 0;
}

bool dvmIsPrimitiveClass(const ClassObject *clazz) {
    return clazz->primitiveType != PRIM_NOT;
}

static void *dvm_dlsym(void *hand, const char *name) {
    void *ret = dlsym(hand, name);
    char msg[1024] = {0};
    snprintf(msg, sizeof(msg) - 1, "0x%x", ret);
#ifdef DEBUG
    LOGD("%s = %s\n", name, msg);
#endif
    return ret;
}

static s8 dvmGetArgLong(const u4 *args, int elem) {
    s8 val;
    memcpy(&val, &args[elem], sizeof(val));
    return val;
}

/*
 * Return a new Object[] array with the contents of "args".  We determine
 * the number and types of values in "args" based on the method signature.
 * Primitive types are boxed.
 *
 * Returns NULL if the method takes no arguments.
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value.
 *
 * On failure, returns with an appropriate exception raised.
 */
ArrayObject *boxMethodArgs(const Method *method, const u4 *args) {
    const char *desc = &method->shorty[1]; // [0] is the return type.

    /* count args */
    size_t argCount = dexProtoGetParameterCount_fnPtr(&method->prototype);

    /* allocate storage */
    ArrayObject *argArray = dvmAllocArrayByClass_fnPtr(classJavaLangObjectArray,
                                                       argCount, ALLOC_DEFAULT);
    if (argArray == NULL)
        return NULL;
    Object **argObjects = (Object **) (void *) argArray->contents;

    /*
     * Fill in the array.
     */

    size_t srcIndex = 0;
    size_t dstIndex = 0;
    while (*desc != '\0') {
        char descChar = *(desc++);
        jvalue value;

        switch (descChar) {
            case 'Z':
            case 'C':
            case 'F':
            case 'B':
            case 'S':
            case 'I':
                value.i = args[srcIndex++];
                argObjects[dstIndex] = (Object *) dvmBoxPrimitive_fnPtr(value,
                                                                        dvmFindPrimitiveClass_fnPtr(descChar));
                /* argObjects is tracked, don't need to hold this too */
                dvmReleaseTrackedAlloc_fnPtr(argObjects[dstIndex], NULL);
                dstIndex++;
                break;
            case 'D':
            case 'J':
                value.j = dvmGetArgLong(args, srcIndex);
                srcIndex += 2;
                argObjects[dstIndex] = (Object *) dvmBoxPrimitive_fnPtr(value,
                                                                        dvmFindPrimitiveClass_fnPtr(descChar));
                dvmReleaseTrackedAlloc_fnPtr(argObjects[dstIndex], NULL);
                dstIndex++;
                break;
            case '[':
            case 'L':
                argObjects[dstIndex++] = (Object *) args[srcIndex++];
#ifdef DEBUG
                LOGD("[boxMethodArgs] : object: index = %d", dstIndex - 1);
#endif
                break;
        }
    }

    return argArray;
}

