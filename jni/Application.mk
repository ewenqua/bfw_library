APP_PROJECT_PATH := $(call my-dir)/..

APP_STL := gnustl_shared
APP_CFLAGS := -O3 -UNDEBUG -g
APP_PLATFORM := android-14
APP_PIE := false
APP_ABI := armeabi-v7a

WESNOTH_RELEASE := $(shell (cat $(APP_PROJECT_PATH)/RELEASE))

