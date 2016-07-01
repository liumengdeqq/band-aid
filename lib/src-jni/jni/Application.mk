# $Id: Application.mk 212 2015-05-15 10:22:36Z oparviai $
#
# Build library bilaries for all supported architectures
#

#APP_PLATFORM := android-11
APP_PLATFORM := android-9
APP_OPTIM := debug
NDK_DEBUG = 1
APP_ABI := armeabi-v7a
APP_CPPFLAGS := -std=c++11 -fexceptions
APP_STL :=stlport_static

