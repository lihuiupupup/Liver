package com.lihui.android.liver.audio;

class VideoData {

    public byte[] data;
    public int time;
    public boolean isHeader;

    public VideoData(byte[] data, int pTime) {
        this.data = data;
        this.time = pTime;

    }

    public VideoData(byte[] teteb, int pTime, boolean b) {
        this.data = data;
        this.time = pTime;
        isHeader = true;
    }
}
