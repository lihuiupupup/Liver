//
// Created by 黎辉 on 11/24/20.
//

#include "PullCallJava.h"


PullCallJava::PullCallJava(JavaVM *vm, JNIEnv *jniEnv, jobject cb) {

    this->javaVm = vm;
    this->jniEnv = jniEnv;
    this->callback = jniEnv->NewGlobalRef(cb);
    this->jcz = jniEnv->FindClass("com/lihui/android/liversdk/LiverNativeCallback");
    this->jm_onSpsPps = jniEnv->GetMethodID(this->jcz, "onSpsPps","(II[B[BII)V");
    this->jm_onAudioHeader = jniEnv->GetMethodID(this->jcz, "onAudioHeader","([BIIII)V");
    this->jm_onVideoData = jniEnv->GetMethodID(this->jcz, "onVideoData", "([BJZ)V");
    this->jm_onAudioData = jniEnv->GetMethodID(this->jcz, "onAudioData", "([BJIII)V");

}

void PullCallJava::onVideoData(char *videoDat, int len, long long int pts, bool isFrameKey) {
    JNIEnv *jniEnv;
    if (this->javaVm->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
        LOGE("onVideoData AttachCurrentThread failed");
    }

    jbyteArray result = jniEnv->NewByteArray(len);
    jniEnv->SetByteArrayRegion(result, 0, len, (jbyte*)(videoDat));
    jniEnv->CallVoidMethod(this->callback,jm_onVideoData ,
                           result,pts,isFrameKey);

    jniEnv->DeleteLocalRef(result);
    javaVm->DetachCurrentThread();
}

void PullCallJava::onAudioData(char *audioData, int len ,long long int pts, int sr, int sb, int c) {
    JNIEnv *jniEnv;
    if (this->javaVm->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
        LOGE("onAudioData AttachCurrentThread failed");
    }
    jbyteArray result = jniEnv->NewByteArray(len);
    jniEnv->SetByteArrayRegion(result, 0, len, (jbyte*)(audioData));
    jniEnv->CallVoidMethod(this->callback, jm_onAudioData,
                           result,pts, sr, sb, c);
    jniEnv->DeleteLocalRef(result);
    javaVm->DetachCurrentThread();
}

void PullCallJava::onAudioHeader(char *data, int len,int sr,int sb,int c) {
    JNIEnv *jniEnv;
    if (this->javaVm->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {

    }
    jbyteArray result = jniEnv->NewByteArray(len);
    jniEnv->SetByteArrayRegion(result, 0, len, (jbyte*)(data));


    jniEnv->CallVoidMethod(this->callback, this->jm_onAudioHeader, result,len,sr,sb,c);

    jniEnv->DeleteLocalRef(result);
    javaVm->DetachCurrentThread();
}

void PullCallJava::onSpsPps(int width, int height, char *sps, char *pps, int spsLen, int ppsLen) {
    JNIEnv *jniEnv;
    if (this->javaVm->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {

    }
    jbyteArray result = jniEnv->NewByteArray(spsLen);
    jniEnv->SetByteArrayRegion(result, 0, spsLen, (jbyte*)(sps));

    jbyteArray result2 = jniEnv->NewByteArray(ppsLen);
    jniEnv->SetByteArrayRegion(result2, 0, ppsLen, (jbyte*)(pps));
    jniEnv->CallVoidMethod(this->callback, this->jm_onSpsPps, width,height,result,result2,spsLen,ppsLen);

    jniEnv->DeleteLocalRef(result2);
    jniEnv->DeleteLocalRef(result);
    javaVm->DetachCurrentThread();
}

PullCallJava::~PullCallJava() {
    callback = NULL;
    callback = NULL;
    javaVm = NULL;
}
