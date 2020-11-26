//
// Created by 黎辉 on 11/23/20.
//

#include <jni.h>
#include "AndroidLog.h"
#include "RtmpPush.h"
#include "RtmpPull.h"

JavaVM *javaVm = NULL;
RtmpPush *rtmpPush = NULL;
RtmpPull *rtmpPull = NULL;
JNIEXPORT int JNICALL init(JNIEnv *env, jobject thiz) {
    int result = 0;
    return result;
}

JNIEXPORT void JNICALL release(JNIEnv *env, jobject thiz) {
}

JNIEXPORT void JNICALL sendVideoData(JNIEnv *env, jobject thiz,jbyteArray videoArr,jint videoLen,jlong videoPts,jboolean isKey) {
    jbyte * videoData = env->GetByteArrayElements(videoArr,NULL);
    if (rtmpPush != NULL) {
        if (isKey) {
            rtmpPush->sendSpsPps();
        }
        rtmpPush->sendVideoData((char *)videoData,videoLen,isKey,videoPts);
    }
    //env->ReleaseByteArrayElements(videoArr,videoData,NULL);
}

JNIEXPORT void JNICALL sendAudioData(JNIEnv *env, jobject thiz,jbyteArray audioArr,jint audioLen,jlong audioPts) {
    jbyte * videoData = env->GetByteArrayElements(audioArr,NULL);
    if (rtmpPush != NULL) {

        rtmpPush->sendAudioData((char *)videoData,audioLen,audioPts);
    }
    //env->ReleaseByteArrayElements(audioArr,videoData,NULL);
}

JNIEXPORT void JNICALL saveSpsPps(JNIEnv *env, jobject thiz,jbyteArray spsArr,jint spsLen,jbyteArray ppsArr,jint ppsLen) {

    jbyte * spsdata = env->GetByteArrayElements(spsArr,NULL);

    jbyte * ppsdata = env->GetByteArrayElements(ppsArr,NULL);
    if (rtmpPush != NULL) {
        rtmpPush->saveVideoSpsAndPps((char *)spsdata,(char *)ppsdata,spsLen,ppsLen);
    }
    //env->ReleaseByteArrayElements(spsArr,spsdata,NULL);
    //env->ReleaseByteArrayElements(ppsArr,ppsdata,NULL);
}

JNIEXPORT void JNICALL saveAudioHeader(JNIEnv *env, jobject thiz,jbyteArray ahArr,jint ahLen) {
    jbyte  * data = env->GetByteArrayElements(ahArr,NULL);
    if (rtmpPush != NULL) {
        rtmpPush->saveAudioHeader(reinterpret_cast<char *>(data), ahLen);
    }
    //env->ReleaseByteArrayElements(ahArr,data,NULL);
}

JNIEXPORT int JNICALL startPush(JNIEnv *env, jobject thiz,jstring url) {
    rtmpPush = new RtmpPush;
    char * pushUrl = const_cast<char *>(env->GetStringUTFChars(url, NULL));
    if (rtmpPush != NULL) {
        int result = rtmpPush->startPush(pushUrl);
        if (result == 0) {
            return 0;
        } else { //连接释放对象
            delete rtmpPush;
            rtmpPush = NULL;
            return -1;
        }
    }
    return -1;
    //env->ReleaseStringUTFChars(url,pushUrl);
}

JNIEXPORT void JNICALL stopPush(JNIEnv *env, jobject thiz) {
    if (rtmpPush != NULL) {
        rtmpPush->stopPush();
        delete rtmpPush;
        rtmpPush = NULL;
    }
}

JNIEXPORT int JNICALL startPull(JNIEnv *env, jobject thiz,jstring url,jobject callback) {
    char *pullUrl = (char *)env->GetStringUTFChars(url,NULL);
    rtmpPull = new RtmpPull();
    int result = rtmpPull->startPull(javaVm,env,pullUrl,callback);
    if (result == 0) {

        return 0;
    } else {
        delete rtmpPull;
        return -1;

    }
}
JNIEXPORT void JNICALL stopPull(JNIEnv *env, jobject thiz) {

    if (rtmpPull != NULL) {
        rtmpPull->stopPull();
        delete  rtmpPull;
        rtmpPull = NULL;
    }
}
JNINativeMethod  nativeMethod[] = {
        {"init","()I",(void *)init},
        {"release","()V",(void *)release},
        {"sendVideoData","([BIJZ)V",(void *)sendVideoData},
        {"sendAudioData","([BIJ)V",(void *)sendAudioData},
        {"saveSpsPps","([BI[BI)V",(void *)saveSpsPps},
        {"saveAudioHeader","([BI)V",(void *)saveAudioHeader},
        {"startPush","(Ljava/lang/String;)I",(void *)startPush},
        {"stopPush","()V",(void *)stopPush},
        {"startPull","(Ljava/lang/String;Lcom/lihui/android/liversdk/LiverNativeCallback;)I",(void *)startPull},
        {"stopPull","()V",(void *)stopPull}
};

void registerMyNativeMethod(JNIEnv *jniEnv) {

    jclass liveClazz = jniEnv->FindClass("com/lihui/android/liversdk/LiverNative");
    jniEnv->RegisterNatives(liveClazz,nativeMethod,sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}
extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    jint result = -1;
    javaVm = vm;
    JNIEnv *jniEnv = NULL;
    if ((result = javaVm->GetEnv((void **) (&jniEnv), JNI_VERSION_1_6)) != JNI_OK) {
        LOGE("GetEnv ERROR");
        return result;
    }
    registerMyNativeMethod(jniEnv);
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {


    javaVm = NULL;
}

