//
// Created by 黎辉 on 11/24/20.
//

#ifndef LIVER_RTMPPUSH_H
#define LIVER_RTMPPUSH_H
#include <queue>
#include "AndroidLog.h"
#include <string.h>
extern "C" {
#include "librtmp/rtmp.h"
#include <pthread.h>
}
class RtmpPush {
    std::queue<RTMPPacket *> myQueue;
    pthread_t pthread;
    bool pThreadCreated = false;
    pthread_mutex_t pthreadMutex;
    pthread_cond_t pthreadCond;

    char *sps;
    char *pps;
    int spsLen;
    int ppsLen;
    char *audioHeader;
    int ahLen;

    int audioHeaderCount = 0;
    void addPacket(RTMPPacket * packet);
    void release();
    void sendAudioData(char *data,int len,bool isInfo,long long pts);

public:
    RTMP *rtmp;
    char *pushUrl;
    bool isPushStarted = false;
    int startPush(char *url);
    void sendSpsPps();
    void stopPush();

    void sendVideoData(char *data,int len,bool isKeyFrame,long long pts);

    void sendAudioData(char *data,int len,long long pts);

    void saveVideoSpsAndPps(char *sps,char *pps,int spsLen,int ppsLen);

    void saveAudioHeader(char *audioInfo,int audioInfoLen);
    RTMPPacket * getPacket();

    ~RtmpPush();
    long startLivingTime;
    bool isReadyofThread = false;
};


#endif //LIVER_RTMPPUSH_H
