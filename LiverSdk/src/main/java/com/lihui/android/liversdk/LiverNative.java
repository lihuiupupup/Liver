package com.lihui.android.liversdk;

class LiverNative {
    static {
        System.loadLibrary("liverSdk");
    }
    public native int init();

    public native void release();

    public native void sendVideoData(byte[] data,int len,long pts,boolean isKeyFrame);

    public native void sendAudioData(byte[] data,int len,long pts);

    public native void saveSpsPps(byte[] sps,int spsLen,byte[] pps,int ppsLen);

    public native void saveAudioHeader(byte[] audioHeader,int len);


    public native int startPush(String url);

    public native void stopPush();

    public native int startPull(String url,LiverNativeCallback callback);

    public native void stopPull();
}
