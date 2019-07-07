#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_csu_liutao_ffmpegmgr_FfmpegMgr_getStringFromNative(JNIEnv *env, jobject instance, jint order) {

    return env->NewStringUTF("hello 666");
}