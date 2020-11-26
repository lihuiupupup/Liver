//
// Created by 黎辉 on 11/24/20.
//

#ifndef LIVER_RTMPPULL_H
#define LIVER_RTMPPULL_H
#include "PullCallJava.h"
#include <jni.h>
#include "ParseSps.h"
#include <string.h>
extern "C" {
#include "librtmp/rtmp.h"
#include "pthread.h"
}
class RtmpPull {



public:
    int startPull(JavaVM * javaVm ,JNIEnv * jniEnv, char *pullUrl,jobject cb);
    RTMP *rtmp = NULL;
    bool isPullStarted = false;
    ~RtmpPull();
    char *url = NULL;
    void stopPull() ;
    pthread_t pull_thread;
    bool pThreadCreated = false;
    PullCallJava * pullCallJava = NULL;
    void release();

};


#endif //LIVER_RTMPPULL_H
