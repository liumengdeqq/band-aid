LOCAL_PATH:= $(call my-dir)/../../

include $(CLEAR_VARS)

LOCAL_MODULE:= band-aid

LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/jni/prebuilt/include

LOCAL_SRC_FILES:= HotPatch.cpp\
                  dalvik/dalvik.cpp\
                  dalvik/dalvik_method_dispatch.cpp\
                  dalvik/dalvik_method_replace.cpp\
                  art/art.cpp\
                  art/art_method_replace.cpp\
                  art/art_method_dispatch.cpp\
                  art/art_quick_dexposed_invoke_handler.S\
                  ArtBridge.cpp\
                  DalvikBridge.cpp

LOCAL_STATIC_LIBRARIES := hook

LOCAL_LDLIBS := -llog -landroid

LOCAL_CFLAGS := -Wliteral-suffix -fpermissive -DDEBUG

#LOCAL_LDFLAGS = D:/sdk/android-ndk-r10d/platforms/android-9/arch-arm/usr/lib/libstdc++.a

#LOCAL_CPPFLAGS += -fexceptions -lstdc++

LOCAL_CPP_EXTENSION := .cc .cxx .cpp

include $(BUILD_SHARED_LIBRARY)
