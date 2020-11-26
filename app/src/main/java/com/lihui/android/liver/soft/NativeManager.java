package com.lihui.android.liver.soft;

class NativeManager {

    static {
        System.loadLibrary("liver");
    }

    public native int initCodec();


}
