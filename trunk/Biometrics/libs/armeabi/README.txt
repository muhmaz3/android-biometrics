=OpenCV 2.3.1 compiled for Android 2.2=
==Introduction==

The library files inside the `libs` subdirectory are meant to be used with [http://code.google.com/p/javacv/ JavaCV]. Further, the ones inside the `armeabi-v7a` subdirectory were compiled for ARM processors featuring an FPU, so they will not work on old devices such as the HTC Magic, or inside the Android Emulator. For those cases, please use the ones in the `armeabi` subdirectory.

==Rebuilding Instructions==
Required software:
 * OpenCV 2.3.1  http://sourceforge.net/projects/opencvlibrary/files/
 * Android NDK r5c  http://developer.android.com/sdk/ndk/

I built the included libraries by first applying the included `opencv-android-20110820.patch` to OpenCV 2.3.1 and by running the following commands under Fedora 14:
{{{
ANDROID_BIN=`pwd`/../android-ndk-r5c/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/ \
ANDROID_CPP=`pwd`/../android-ndk-r5c/sources/cxx-stl/gnu-libstdc++/ \
ANDROID_ROOT=`pwd`/../android-ndk-r5c/platforms/android-9/arch-arm/ \
cmake -DCMAKE_TOOLCHAIN_FILE=android.cmake -DCMAKE_INSTALL_PREFIX=.. \
-DOPENCV_BUILD_3RDPARTY_LIBS=TRUE -DWITH_FFMPEG=FALSE -DWITH_GTK=FALSE -DWITH_GSTREAMER=FALSE \
-DWITH_V4L=FALSE -DWITH_PVAPI=FALSE -DWITH_1394=FALSE -DBUILD_NEW_PYTHON_SUPPORT=FALSE -DBUILD_TESTS=FALSE
make
}}}

To compile binaries for a device with no FPU, replace "libs/armeabi-v7a" by "libs/armeabi" and "-march=armv7-a -mfloat-abi=softfp -mfpu=vfp" with "-march=armv5te -mtune=xscale -msoft-float", inside `android.cmake`.

----
Copyright (C) 2011 Samuel Audet <samuel.audet@gmail.com>
Project site: http://code.google.com/p/javacv/
