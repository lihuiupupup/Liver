//
// Created by 黎辉 on 11/24/20.
//

#ifndef LIVER_PULLCALLJAVA_H
#define LIVER_PULLCALLJAVA_H
#include <jni.h>
#include "AndroidLog.h"
class PullCallJava {
    jmethodID jm_onSpsPps;
    jmethodID jm_onAudioHeader;
    jmethodID jm_onVideoData;
    jmethodID jm_onAudioData;
    jclass jcz;
    JNIEnv * jniEnv;
public:
    JavaVM *javaVm = NULL;
    jobject callback = NULL;

    PullCallJava(JavaVM *vm,JNIEnv *jniEnv,jobject cb);

    ~PullCallJava();
    void onVideoData(char *videoDat,int len, long long int pts,bool isFrameKey);

    void onAudioData(char *audioData,int len ,long long int pts,int sr,int sb,int c);

    void onAudioHeader(char *data,int len,int sr,int sb,int c);

    void onSpsPps(int width,int height,char *sps,char *pps,int spsLen,int ppsLen);


};


#endif //LIVER_PULLCALLJAVA_H
