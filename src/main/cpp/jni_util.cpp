#include "jni_util.h"

#include <android/log.h>
#include <cstdlib>

#ifndef LOG_TAG
#define LOG_TAG "FlycastJNI"
#endif

#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

// ------------------------------------------------------------
// REFERENCE ONLY - do NOT define g_jvm here
// ------------------------------------------------------------
extern JavaVM* g_jvm;

// Optional global activity ref
jobject g_activity = nullptr;

namespace jni
{
    JTLS JVMAttacher jvm_attacher;
}

// ------------------------------------------------------------
// Do NOT define fatal_error here if core/ui/gui.cpp already has it
// ------------------------------------------------------------

void os_DebugBreak()
{
    ALOGE("os_DebugBreak() - prevented crash");
}

