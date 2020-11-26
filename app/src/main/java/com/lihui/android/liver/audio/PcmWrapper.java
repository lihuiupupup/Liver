package com.lihui.android.liver.audio;

class PcmWrapper {

    private byte[] d;
    private long pts;

    public PcmWrapper(byte[] d, long pts) {
        this.d = d;
        this.pts = pts;
    }

    public byte[] getD() {
        return d;
    }

    public long getPts() {
        return pts;
    }
}
