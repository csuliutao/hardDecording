
cmake_minimum_required(VERSION 3.4.1)

set(ADD_LIB /home/liutao/Android/projects/FfmpegDemo/ffmpegmgr/src/main/jnilibs)


add_library( # Sets the name of the library.
        native-lib
        SHARED
        native-lib.cpp)


add_library(
        avcodec-58
        SHARED
        IMPORTED
)

set_target_properties(
        avcodec-58
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libavcodec-58.so
)


add_library(
        avdevice-58
        SHARED
        IMPORTED
)

set_target_properties(
        avdevice-58
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libavdevice-58.so
)

add_library(
        avfilter-7
        SHARED
        IMPORTED
)

set_target_properties(
        avfilter-7
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libavfilter-7.so
)

add_library(
        avformat-58
        SHARED
        IMPORTED
)

set_target_properties(
        avformat-58
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libavformat-58.so
)

add_library(
        avutil-56
        SHARED
        IMPORTED
)

set_target_properties(
        avutil-56
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libavutil-56.so
)

add_library(
        postproc-55
        SHARED
        IMPORTED
)

set_target_properties(
        postproc-55
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libpostproc-55.so
)

add_library(
        swresample-3
        SHARED
        IMPORTED
)

set_target_properties(
        swresample-3
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libswresample-3.so
)

add_library(
        swscale-5
        SHARED
        IMPORTED
)

set_target_properties(
        swscale-5
        PROPERTIES IMPORTED_LOCATION
        ${ADD_LIB}/${ANDROID_ABI}/libswscale-5.so
)

include_directories(
        ${ADD_LIB}/include
)


target_link_libraries(
        native-lib
        swscale-5
        swresample-3
        postproc-55
        avutil-56
        avformat-58
        avfilter-7
        avdevice-58
        avcodec-58
        log)