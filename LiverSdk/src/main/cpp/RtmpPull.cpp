//
// Created by 黎辉 on 11/24/20.
//

#include "RtmpPull.h"

void *pullThread(void *pVoid) {
    RtmpPull *pullRtmp = static_cast<RtmpPull *>(pVoid);

    pullRtmp->isPullStarted = true;
    RTMPPacket packet = {0};
    LOGE("connect Success %s" ,pullRtmp->url);
    while (pullRtmp->isPullStarted) {

        int ret = RTMP_ReadPacket(pullRtmp->rtmp, &packet);

        if (ret && RTMPPacket_IsReady(&packet) && packet.m_nBodySize) {
            int timeStamp = packet.m_nTimeStamp;
            if (packet.m_packetType == RTMP_PACKET_TYPE_VIDEO) {
                char *data = packet.m_body;
                int index = 0;
                bool isKeyFrame = false;
                bool isSpsPps = false;
                isKeyFrame = data[index++] == 0x17;
                isSpsPps = data[index] == 0x00;
                index += 4;
                if (isSpsPps) {
                    index += 6;
                    int a = (data[index++] & 0x000000FF) << 8;
                    int b = (data[index++] & 0x000000FF);
                    int spsLen = a | b;
                    char * sps = new char[spsLen];
                    memcpy(sps,&data[index],spsLen);
                    sps_info_struct spsInfoStruct;
                    h264_parse_sps(&data[index], (unsigned int)spsLen, &spsInfoStruct);
                    index += spsLen;
                    index ++;
                    int c = (data[index++] & 0x000000FF) << 8;
                    int d = (data[index++] & 0x000000FF);
                    int ppsLen = c | d;
                    char * pps = new char[ppsLen];
                    memcpy(pps,&data[index],ppsLen);
                    pullRtmp->pullCallJava->onSpsPps(spsInfoStruct.width,spsInfoStruct.height,sps,pps,spsLen,ppsLen);
                    delete[] sps;
                    delete[] pps;
                } else {

                    int h264DataSize = ((data[index] & 0x000000FF) << 24
                                        | (data[index + 1] & 0x000000FF) << 16
                                        | (data[index + 2] & 0x000000FF) << 8
                                        | (data[index + 3] & 0x000000FF));
                    index += 4;

                    char * result = new char[h264DataSize];
                    memcpy((void *)result, &data[index], h264DataSize);

                    pullRtmp->pullCallJava->onVideoData(result,h264DataSize,timeStamp,isKeyFrame);

                    delete[] result;
                }

            } else if (packet.m_packetType == RTMP_PACKET_TYPE_AUDIO) {

                char info = packet.m_body[0];
                char info2 = packet.m_body[1];
                int simpleRateType = ((info & 0x0000000F) >> 3) << 1 | (info & 0x00000007) >> 2;
                int simpleBitType = (info & 0x00000003) >> 1;
                int channelCountType = (info & 0x00000001);
                if (info2 == 0) {
                    char * result = new char [packet.m_nBodySize -2];
                    memcpy(result,&packet.m_body[2],packet.m_nBodySize-2);
                    pullRtmp->pullCallJava->onAudioHeader(result,packet.m_nBodySize-2,simpleRateType,simpleBitType,channelCountType);
                    delete[] result;
                } else {

                    char * result = new char [packet.m_nBodySize -2];
                    memcpy(result,&packet.m_body[2],packet.m_nBodySize-2);
                    pullRtmp->pullCallJava->onAudioData(result,packet.m_nBodySize-2,timeStamp,simpleRateType,simpleBitType,channelCountType);
                    delete[] result;
                }

            }

        } else {
            continue;
        }

        RTMPPacket_Free(&packet);
    }
    pullRtmp->release();
    LOGE("pull thread finish");
    pthread_exit(&pullRtmp->pull_thread);

}
int RtmpPull::startPull(JavaVM * javaVm ,JNIEnv * jniEnv,char *pullUrl, jobject cb) {
    this->url = pullUrl;
    pullCallJava = new PullCallJava(javaVm,jniEnv,cb);

    this->rtmp = RTMP_Alloc();
    RTMP_Init(this->rtmp);
    this->rtmp->Link.timeout = 10;
    this->rtmp->Link.lFlags |= RTMP_LF_LIVE;
    RTMP_SetupURL(this->rtmp, this->url);

    int count = 0;
    while (count < 3) {
        if (!RTMP_Connect(this->rtmp, NULL)) {
            LOGI("RtmpPush can not connect  url: %s", this->url);
        } else {
            LOGI("RtmpPush connect success, url: %s", this->url);
            break;
        }
        count++;
    }

    count = 0;
    while (count < 3) {
        if (!RTMP_ConnectStream(this->rtmp, 10)) {
            LOGI("RtmpPush can not connect stream url: %s", this->url);
        } else {
            LOGI("RtmpPush connect stream success url: %s", this->url);
            break;
        }
        count++;
    }
    if (count < 3) {
        pthread_create(&pull_thread, NULL, pullThread, this);
        pThreadCreated = true;
        return 0;
    } else {
        if (this->rtmp != NULL) {
            RTMP_Close(this->rtmp);
            RTMP_Free(this->rtmp);
            this->rtmp = NULL;
        }
        return -1;
    }

}

RtmpPull::~RtmpPull() {
    release();
}

void RtmpPull::release() {
    if (pullCallJava != NULL) {
        delete pullCallJava;
        pullCallJava = NULL;
    }
}

void RtmpPull::stopPull() {
    isPullStarted = false;
    if (pthread_kill(pull_thread,0) == 0 && pThreadCreated) {
        pthread_join(pull_thread,NULL);
    }

    if (this->rtmp != NULL) {
        RTMP_Close(this->rtmp);
        RTMP_Free(this->rtmp);
        this->rtmp = NULL;
    }
    LOGE("stop pull Success");

}
