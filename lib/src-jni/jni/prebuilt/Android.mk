LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := hook

LOCAL_SRC_FILES := libhook.a

include $(PREBUILT_STATIC_LIBRARY)