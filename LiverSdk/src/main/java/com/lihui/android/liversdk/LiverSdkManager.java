package com.lihui.android.liversdk;

import android.content.Context;

public class LiverSdkManager {

    private static LiverSdkManager liverSdkManager = new LiverSdkManager();

    private LiverNative liverNative;


    public static LiverSdkManager getLiverSdkManager() {
        return liverSdkManager;
    }

    public int init(Context context) {
        liverNative = new LiverNative();
        return 0;
    }

    public int  startPush(String url,ConnectUrlCallback connectUrlCallback) {
        if (liverNative != null) {

            new Thread(() -> {
                int startPush = liverNative.startPush(url);
                if (connectUrlCallback != null) {
                    connectUrlCallback.onConnectResult(startPush);
                }
            }).start();
        }
        return -1;
    }

    public void sendVideoData(byte[] data,int len,long pts,boolean isKeyFrame) {
        if (liverNative != null) {
            liverNative.sendVideoData(data,len,pts,isKeyFrame);
        }
    }

    public void sendAudioData(byte[] data,int len,long pts) {
        if (liverNative != null) {
            liverNative.sendAudioData(data,len,pts);
        }
    }

    public void saveAudioHeader(byte[] audioHeader,int len) {
        if (liverNative != null) {
            liverNative.saveAudioHeader(audioHeader,len);
        }
    }

    public void saveSpsPps(byte[] sps,int spsLen,byte[] pps,int ppsLen) {
        if (liverNative != null) {
            liverNative.saveSpsPps(sps,spsLen,pps,ppsLen);
        }
    }

    public void stopPush() {
        if (liverNative != null) {
            liverNative.stopPush();
        }
    }

    public int startPull(String url,LiverNativeCallback liverNativeCallback,ConnectUrlCallback connectUrlCallback) {

        if (liverNative != null) {
            new Thread(() -> {
                int startPush = liverNative.startPull(url,liverNativeCallback);
                if (connectUrlCallback != null) {
                    connectUrlCallback.onConnectResult(startPush);
                }
            }).start();
        }
        return -1;
    }

    public void stopPull() {
        if (liverNative != null) {
            liverNative.stopPull();
        }
    }

}
