cmake_minimum_required(VERSION 3.2)
project(hotpatch)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -D__STDC_CONSTANT_MACROS -D__STDC_FORMAT_MACROS -fpermissive")
set(CMAKE_NDK_ROOT D:/sdk/android-ndk-r10d)

include_directories(${CMAKE_NDK_ROOT}/platforms/android-9/arch-arm/usr/include
                    ${CMAKE_NDK_ROOT}/platforms/android-9/arch-arm/usr/include/EGL
                    ${CMAKE_NDK_ROOT}/sources/cxx-stl/stlport/stlport
                    jni/prebuilt/include
                    )

set(SOURCE_FILES
    Patch.cpp
    DalvikBridge.cpp
    dalvik/dalvik.cpp
    dalvik/dalvik_method_dispatch.cpp
    dalvik/dalvik_method_replace.cpp
    ArtBridge.cpp
    art/art.cpp
    art/art_method_dispatch.cpp
    art/art_method_replace.cpp
    )

add_executable(hotpatch ${SOURCE_FILES})