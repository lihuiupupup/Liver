package com.lihui.android.liversdk;

public interface LiverNativeCallback {

    void onVideoData(byte[]vData,long pts,boolean isKeyFrame);
    void onAudioData(byte[]aData,long pts,int sr,int sb,int c);
    void onAudioHeader(byte[] data,int len,int sr,int sb,int c);
    void onSpsPps(int width,int height,byte[] sps,byte[] pps,int spsLen,int ppsLen);
}
