#include <jni.h>
#include <android/log.h>

#define LOG_TAG "FlycastJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Needed by jni_util / android_input
JavaVM* g_jvm = nullptr;

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void*) {
    g_jvm = vm;
    LOGI("JNI_OnLoad: zamdc loaded");
    return JNI_VERSION_1_6;
}

// =============================
// ZamJNIdc native bridge
// =============================

extern "C"
JNIEXPORT jint JNICALL
Java_com_flycast_emulator_emu_VGamepad_layoutHitTestNative(
        JNIEnv* env,
        jobject thiz,
        jfloat x,
        jfloat y) {

    jclass cls = env->GetObjectClass(thiz);
    if (cls == nullptr)
        return -1;

    jmethodID mid = env->GetMethodID(cls, "layoutHitTest", "(FF)I");
    if (mid == nullptr) {
        env->DeleteLocalRef(cls);
        return -1;
    }

    jint result = env->CallIntMethod(thiz, mid, x, y);

    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        result = -1;
    }

    env->DeleteLocalRef(cls);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_renderNative(
        JNIEnv* env,
        jclass clazz) {

    LOGI("ZamJNIdc.renderNative");

    // Stub for now.
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_initEnvironment(
        JNIEnv* env,
        jclass clazz,
        jobject activity,
        jstring filesDir,
        jstring homeDir,
        jstring locale) {

    const char* cFiles =
            filesDir ? env->GetStringUTFChars(filesDir, nullptr) : nullptr;

    const char* cHome =
            homeDir ? env->GetStringUTFChars(homeDir, nullptr) : nullptr;

    const char* cLoc =
            locale ? env->GetStringUTFChars(locale, nullptr) : nullptr;

    LOGI("ZamJNIdc.initEnvironment filesDir=%s homeDir=%s locale=%s",
         cFiles ? cFiles : "(null)",
         cHome ? cHome : "(null)",
         cLoc ? cLoc : "(null)");

    if (filesDir && cFiles)
        env->ReleaseStringUTFChars(filesDir, cFiles);

    if (homeDir && cHome)
        env->ReleaseStringUTFChars(homeDir, cHome);

    if (locale && cLoc)
        env->ReleaseStringUTFChars(locale, cLoc);
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_setGameUri(
        JNIEnv* env,
        jclass clazz,
        jstring path,
        jboolean start) {

    const char* cpath =
            path ? env->GetStringUTFChars(path, nullptr) : nullptr;

    LOGI("ZamJNIdc.setGameUri path=%s start=%d",
         cpath ? cpath : "(null)",
         start ? 1 : 0);

    if (path && cpath) {
        env->ReleaseStringUTFChars(path, cpath);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_bootGame(
        JNIEnv* env,
        jclass clazz,
        jstring path) {

    const char* cpath =
            path ? env->GetStringUTFChars(path, nullptr) : nullptr;

    LOGI("ZamJNIdc.bootGame path=%s",
         cpath ? cpath : "(null)");

    if (path && cpath) {
        env->ReleaseStringUTFChars(path, cpath);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_rendinitNative(
        JNIEnv* env,
        jclass clazz,
        jobject surface,
        jboolean resume) {

    LOGI("ZamJNIdc.rendinitNative surface=%p resume=%d",
         surface,
         resume ? 1 : 0);

    // Stub for now.
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_screenResize(
        JNIEnv* env,
        jclass clazz,
        jint width,
        jint height) {

    LOGI("ZamJNIdc.screenResize width=%d height=%d",
         (int) width,
         (int) height);

    // Stub for now.
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_rendtermNative(
        JNIEnv* env,
        jclass clazz) {

    LOGI("ZamJNIdc.rendtermNative");

    // Stub for now.
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_resumeNative(
        JNIEnv* env,
        jclass clazz) {

    LOGI("ZamJNIdc.resumeNative");

    // Stub for now.
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_pauseNative(
        JNIEnv* env,
        jclass clazz) {

    LOGI("ZamJNIdc.pauseNative");

    // Stub for now.
}

extern "C" JNIEXPORT void JNICALL
Java_com_reicast_emulator_emu_JNIdc_stopNative(
        JNIEnv* env,
        jclass clazz) {

    LOGI("ZamJNIdc.stopNative");

    // Stub for now.
}
