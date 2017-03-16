LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := wesnoth-$(WESNOTH_RELEASE)

APPDIR := $(LOCAL_PATH)/src

APP_SUBDIRS := $(patsubst $(LOCAL_PATH)/%, %, $(shell find $(APPDIR) -type d -print))

LOCAL_SRC_FILES := $(filter %.c %.cpp, $(APP_SUBDIRS))
APP_SUBDIRS := $(filter-out %.c %.cpp, $(APP_SUBDIRS))

LOCAL_SRC_FILES += $(foreach F, $(APP_SUBDIRS), $(addprefix $(F)/,$(notdir $(wildcard $(LOCAL_PATH)/$(F)/*.cpp))))
LOCAL_SRC_FILES += $(foreach F, $(APP_SUBDIRS), $(addprefix $(F)/,$(notdir $(wildcard $(LOCAL_PATH)/$(F)/*.c))))

LOCAL_CFLAGS := -I$(LOCAL_PATH)/src -I$(LOCAL_PATH)/include -I$(LOCAL_PATH)/include/glib-2.0 -frtti -fexceptions -D_GNU_SOURCE=1 -DDISABLE_POOL_ALLOC -DWESNOTH_PATH=\".\" -DLOCALEDIR=\"translations\" -DFIFODIR=\"./var/run/wesnothd\" -DWESNOTH_PREFIX=\".\" -fexceptions -frtti

LOCAL_CPP_EXTENSION := .cpp

PREBUILT_LDLIBS := $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libboost.a $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libbzip2.a $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libharfbuzz.a $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libpango.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libcairo.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libfontconfig.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libbfw_png.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libbfw_pixman.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libbfw_glib.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libfreetype.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libbfw_expat.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libffi.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libintl.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libsdl_sound.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libsdl_net.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libsdl_mixer.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libsdl_ttf.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libsdl_image.so $(LOCAL_PATH)/prebuilt-$(TARGET_ARCH_ABI)/libsdl-1.2.so

LOCAL_LDLIBS := -lGLESv1_CM -ldl -llog -lz
LOCAL_LDLIBS += $(PREBUILT_LDLIBS)

include $(BUILD_SHARED_LIBRARY)

include $(LOCAL_PATH)/main/Android.mk


