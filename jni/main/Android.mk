
include $(CLEAR_VARS)

LOCAL_MODULE := main-$(WESNOTH_RELEASE)

#ifndef SDL_JAVA_PACKAGE_PATH
SDL_JAVA_PACKAGE_PATH := it_ap_wesnoth
#endif

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_CFLAGS := -DSDL_JAVA_PACKAGE_PATH=$(SDL_JAVA_PACKAGE_PATH) -DSDL_CURDIR_PATH=\"$(SDL_CURDIR_PATH)\"

LOCAL_CPP_EXTENSION := .cpp

LOCAL_SRC_FILES := main/sdl_main.c

LOCAL_SHARED_LIBRARIES := sdl-$(SDL_VERSION) wesnoth-$(WESNOTH_RELEASE)
LOCAL_LDLIBS := -llog -lz $(PREBUILT_LDLIBS)

include $(BUILD_SHARED_LIBRARY)
