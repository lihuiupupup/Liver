//
// Created by 黎辉 on 11/24/20.
//

#include "RtmpPush.h"

void * pushThread(void * obj) {

    RtmpPush * rtmpPush = static_cast<RtmpPush *>(obj);
    rtmpPush->startLivingTime = RTMP_GetTime();
    rtmpPush->isReadyofThread = true;
    while (rtmpPush->isPushStarted) {
        RTMPPacket *packet = rtmpPush->getPacket();
        if (packet == NULL) {
            continue;
        }
        int result = RTMP_SendPacket(rtmpPush->rtmp, packet, 1);
        LOGI("RtmpPush RTMP_SendPacket result: %d", result);
        RTMPPacket_Free(packet);
        free(packet);
        packet = NULL;
    }

    if (rtmpPush->rtmp != NULL) {
        RTMP_Close(rtmpPush->rtmp);
        RTMP_Free(rtmpPush->rtmp);
        rtmpPush->rtmp = NULL;
    }
    LOGI("push thread finished");
    rtmpPush->isReadyofThread = false;
    pthread_exit(NULL);

}


int RtmpPush::startPush(char *url) {

    isPushStarted = true;
    this->pushUrl = url;
    pthread_mutex_init(&pthreadMutex, nullptr);
    pthread_cond_init(&pthreadCond, nullptr);

    this->rtmp = RTMP_Alloc();
    RTMP_Init(this->rtmp);
    this->rtmp->Link.timeout = 10;
    this->rtmp->Link.lFlags |= RTMP_LF_LIVE;
    RTMP_SetupURL(this->rtmp, this->pushUrl);
    RTMP_EnableWrite(this->rtmp);

    int count = 0;
    while (count < 3) {
        if (rtmp == NULL) {
            break;
        }
        if (!RTMP_Connect(this->rtmp, NULL)) {
            LOGI("RtmpPush can not connect  url: %s", this->pushUrl);
        } else {
            LOGI("RtmpPush connect success, url: %s", this->pushUrl);
            break;
        }
        count++;
    }

    count = 0;
    while (count < 3) {
        if (rtmp == NULL) {
            break;
        }
        if (!RTMP_ConnectStream(this->rtmp, 10)) {
            LOGI("RtmpPush can not connect stream url: %s", this->pushUrl);
        } else {
            LOGI("RtmpPush connect stream success url: %s", this->pushUrl);
            break;
        }
        count++;
    }
    if (count < 3 && rtmp != NULL) {
        pthread_create(&pthread, NULL, pushThread, this);
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
RtmpPush::~RtmpPush() {

    release();
}


void RtmpPush::stopPush() {
    isPushStarted = false;
    pthread_mutex_lock(&pthreadMutex);
    pthread_cond_signal(&pthreadCond);
    pthread_mutex_unlock(&pthreadMutex);
    if (pThreadCreated && pthread_kill(pthread,0) == 0) {
        pthread_join(pthread, NULL);
    }
}


void RtmpPush::saveVideoSpsAndPps(char *sps, char *pps, int spsLen, int ppsLen) {

    this->sps = sps;
    this->pps = pps;
    this->spsLen = spsLen;
    this->ppsLen = ppsLen;
}

void RtmpPush::saveAudioHeader(char *audioInfo, int audioInfoLen) {

    this->audioHeader = audioInfo;
    this->ahLen = audioInfoLen;
}


void RtmpPush::sendVideoData(char *data, int len, bool isKeyFrame, long long int pts) {
    if (!isPushStarted || !isReadyofThread) {
        return;
    }
    int bodySize = len + 9;
    RTMPPacket *rtmpPacket = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(rtmpPacket, bodySize);
    RTMPPacket_Reset(rtmpPacket);

    char *body = rtmpPacket->m_body;

    int i = 0;
    if (isKeyFrame) {
        body[i++] = 0x17;
    } else {
        body[i++] = 0x27;
    }

    body[i++] = 0x01;
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    body[i++] = (len >> 24) & 0xff;
    body[i++] = (len >> 16) & 0xff;
    body[i++] = (len >> 8) & 0xff;
    body[i++] = len & 0xff;

    //data
    memcpy(&body[i], data, len);

    rtmpPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    rtmpPacket->m_nBodySize = bodySize;
    //持续播放时间
    rtmpPacket->m_nTimeStamp = RTMP_GetTime() - startLivingTime;
    //rtmpPacket->m_nTimeStamp = pts / 1000;
    //进入直播播放开始时间
    rtmpPacket->m_hasAbsTimestamp = 0;
    rtmpPacket->m_nChannel = 0x04;//音频或者视频
    rtmpPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    rtmpPacket->m_nInfoField2 = this->rtmp->m_stream_id;
    LOGI("RtmpPush send video size %d", len);
    addPacket(rtmpPacket);
}

void RtmpPush::sendAudioData(char *data, int len, bool isInfo, long long int pts) {
    if (!isPushStarted || !isReadyofThread) {
        return;
    }
    int bodySize = len + 2;
    RTMPPacket *rtmpPacket = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(rtmpPacket, bodySize);
    RTMPPacket_Reset(rtmpPacket);

    char *body = rtmpPacket->m_body;
    //前四位表示音频数据格式  10（十进制）表示AAC，16进制就是A
    //第5-6位的数值表示采样率，0 = 5.5 kHz，1 = 11 kHz，2 = 22 kHz，3(11) = 44 kHz。
    //第7位表示采样精度，0 = 8bits，1 = 16bits。
    //第8位表示音频类型，0 = mono，1 = stereo
    //这里是44100 立体声 16bit 二进制就是1111   16进制就是F
    body[0] = 0xAF;

    //0x00 aac头信息     0x01 aac 原始数据
    //这里都用0x01都可以
    if (isInfo) {
        body[1] = 0x00;

    } else {
        body[1] = 0x01;
    }

    //data
    memcpy(&body[2], data, len);

    rtmpPacket->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    rtmpPacket->m_nBodySize = bodySize;
    //持续播放时间
    rtmpPacket->m_nTimeStamp = RTMP_GetTime() - startLivingTime;
    //rtmpPacket->m_nTimeStamp = pts / 1000;
    //进入直播播放开始时间
    rtmpPacket->m_hasAbsTimestamp = 0;
    rtmpPacket->m_nChannel = 0x04;//音频或者视频
    rtmpPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    rtmpPacket->m_nInfoField2 = this->rtmp->m_stream_id;
    LOGI("RtmpPush add audio szie %d", len);
    addPacket(rtmpPacket);
}

RTMPPacket *RtmpPush::getPacket() {
    pthread_mutex_lock(&pthreadMutex);
    RTMPPacket *rtmpPacket = NULL;
    if (!myQueue.empty()) {
        rtmpPacket = myQueue.front();
        myQueue.pop();
    } else {
        pthread_cond_wait(&pthreadCond, &pthreadMutex);
    }
    pthread_mutex_unlock(&pthreadMutex);
    return rtmpPacket;
}

void RtmpPush::addPacket(RTMPPacket *packet) {
    pthread_mutex_lock(&pthreadMutex);
    myQueue.push(packet);
    pthread_cond_signal(&pthreadCond);
    pthread_mutex_unlock(&pthreadMutex);
}

void RtmpPush::release() {
    pthread_mutex_lock(&pthreadMutex);
    while (true) {
        if (myQueue.empty()) {
            break;
        }
        RTMPPacket *rtmpPacket = myQueue.front();
        myQueue.pop();
        RTMPPacket_Free(rtmpPacket);
        rtmpPacket = NULL;
    }
    pthread_mutex_unlock(&pthreadMutex);
    pthread_mutex_destroy(&pthreadMutex);
    pthread_cond_destroy(&pthreadCond);
    if (this->rtmp != NULL) {
        RTMP_DeleteStream(rtmp);
        RTMP_Close(this->rtmp);
        RTMP_Free(this->rtmp);
        this->rtmp = NULL;
    }

}

void RtmpPush::sendSpsPps() {
    if (!isPushStarted || !isReadyofThread) {
        return;
    }

    char *sps = this->sps;
    char *pps = this->pps;
    int spsLen = this->spsLen;
    int ppsLen = this->ppsLen;
    int bodySize = spsLen + ppsLen + 16;

    RTMPPacket *rtmpPacket = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(rtmpPacket, bodySize);
    RTMPPacket_Reset(rtmpPacket);


    char *body = rtmpPacket->m_body;

    int i = 0;
    //frame type(4bit)和CodecId(4bit)合成一个字节(byte)
    //frame type 关键帧1  非关键帧2
    //CodecId  7表示avc
    body[i++] = 0x17;

    //fixed 4byte
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    //configurationVersion： 版本 1byte
    body[i++] = 0x01;

    //AVCProfileIndication：Profile 1byte  sps[1]
    body[i++] = sps[1];

    //compatibility：  兼容性 1byte  sps[2]
    body[i++] = sps[2];

    //AVCLevelIndication： ProfileLevel 1byte  sps[3]
    body[i++] = sps[3];

    //lengthSizeMinusOne： 包长数据所使用的字节数  1byte
    body[i++] = 0xff;

    //sps个数 1byte
    body[i++] = 0xe1;
    //sps长度 2byte
    body[i++] = (spsLen >> 8) & 0xff;
    body[i++] = spsLen & 0xff;

    //sps data 内容
    memcpy(&body[i], sps, spsLen);
    i += spsLen;
    //pps个数 1byte
    body[i++] = 0x01;
    //pps长度 2byte
    body[i++] = (ppsLen >> 8) & 0xff;
    body[i++] = ppsLen & 0xff;
    //pps data 内容
    memcpy(&body[i], pps, ppsLen);

    rtmpPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    rtmpPacket->m_nBodySize = bodySize;
    rtmpPacket->m_nTimeStamp = 0;
    rtmpPacket->m_hasAbsTimestamp = 0;
    rtmpPacket->m_nChannel = 0x04;//音频或者视频
    rtmpPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    rtmpPacket->m_nInfoField2 = this->rtmp->m_stream_id;
    LOGI("RtmpPush add sps szie %d", spsLen);
    addPacket(rtmpPacket);

}

void RtmpPush::sendAudioData(char *data, int len, long long int pts) {
    if (!isPushStarted || !isReadyofThread) {
        return;
    }
    if (audioHeaderCount == 5) {
        audioHeaderCount = 0;
    }
    if (audioHeaderCount == 0) {
        sendAudioData(audioHeader, ahLen, true,pts);
    }

    audioHeaderCount++;
    sendAudioData(data, len, false,pts);

}
