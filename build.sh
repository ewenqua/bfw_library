#!/bin/sh

cd $(dirname $0)

#./wesnoth-*/copy_src.sh || exit
ndk-build || exit
#cp jni/prebuilt-armeabi/lib*.so libs/armeabi/ || exit
cp jni/prebuilt-armeabi-v7a/lib*.so libs/armeabi-v7a/ || exit


