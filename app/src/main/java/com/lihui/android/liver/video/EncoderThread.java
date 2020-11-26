package com.lihui.android.liver.video;


import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.lihui.android.liver.activity.PushActivity2;
import com.lihui.android.liver.filter.EglCore;
import com.lihui.android.liver.filter.ToScreenFilter;
import com.lihui.android.liver.filter.WindowSurface;
import com.lihui.android.liversdk.LiverSdkManager;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EncoderThread {

    private static final String TAG = "EncoderThread";
    private EGLContext eglCtx;
    private EglCore eglCore;
    public WindowSurface windowSurface;
    private Surface surface;
    private MediaCodec mediaCodec;
    private String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private MediaCodec.BufferInfo bufferInfo;
    private int mTextureId;

    private int w;
    private int h;
    private boolean isStart = false;
    private ToScreenFilter toScreenFilter;

    @SuppressLint("NewApi")
    public EncoderThread(EGLContext sharedContext, int width, int height, int textureId) {
        eglCtx = sharedContext;
        mTextureId = textureId;
        this.w = width;
        this.h = height;
        try {

            bufferInfo = new MediaCodec.BufferInfo();
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, PushActivity2.BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            surface = mediaCodec.createInputSurface();
            mediaCodec.start();
            Log.e(TAG, "codec init success");
        } catch (IOException e) {
            e.printStackTrace();
        }

        HandlerThread encoderThread = new HandlerThread("encoderThread") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                eglCore = new EglCore(eglCtx, EglCore.FLAG_RECORDABLE);
                windowSurface = new WindowSurface(eglCore, surface, true);
                windowSurface.makeCurrent();
                toScreenFilter = new ToScreenFilter(mTextureId);
                toScreenFilter.setWH(w, h);

            }
        };
        encoderThread.start();

        handler = new Handler(encoderThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                long pts = (long) msg.obj;
                toScreenFilter.draw();
                windowSurface.setPresentationTime(pts);
                windowSurface.swapBuffers();
            }
        };

//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            fileOutputStream = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

    }

//    private File file = new File(Environment.getExternalStorageDirectory(),"ah264.h264");
//    private FileOutputStream fileOutputStream;

    public void start() {

        if (isStart) {
            return;
        }
        isStart = true;

        new Thread(() -> {

            encoderTime2  = System.currentTimeMillis();
            ByteBuffer byteBuffer;
            while (isStart) {
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
                if (outputBufferIndex == -2) {
                    ByteBuffer spsb = mediaCodec.getOutputFormat().getByteBuffer("csd-0");
                    spsb.position(4);
                    byte[] sps = new byte[spsb.remaining()];
                    spsb.get(sps, 0, sps.length);

                    ByteBuffer ppsb = mediaCodec.getOutputFormat().getByteBuffer("csd-1");
                    ppsb.position(4);
                    byte[] pps = new byte[ppsb.remaining()];
                    ppsb.get(pps, 0, pps.length);
                    LiverSdkManager.getLiverSdkManager().saveSpsPps(sps,sps.length,pps,pps.length);
                }
                if (outputBufferIndex >= 0) {
                    byteBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];
                    byteBuffer.position(bufferInfo.offset);
                    byteBuffer.limit(bufferInfo.offset + bufferInfo.size);


                    byte[] bytes = new byte[5];
                    byte[] bytes2 = new byte[byteBuffer.remaining() - 4];
                    byteBuffer.get(bytes, 0, 5);

                    byteBuffer.position(4);
                    byteBuffer.get(bytes2, 0, byteBuffer.remaining());
                    boolean iskey = false;


                    int k = 0;
                    if (bytes[k] == 0x00 && bytes[k + 1] == 0x00 && bytes[k + 2] == 0x01 && bytes[k + 3] == 0x65) {
                        Log.e(TAG, "sps iskey " + iskey);
                        iskey = true;
                    }

                    if (bytes[k] == 0x00 && bytes[k + 1] == 0x00 && bytes[k + 2] == 0x00 && bytes[k + 3] == 0x01 && bytes[k + 4] == 0x65) {
                        Log.e(TAG, "sps iskey " + iskey);
                        iskey = true;
                    }
                    Log.e(TAG,"h264 size" + bytes2.length + " " + bufferInfo.presentationTimeUs);

                    LiverSdkManager.getLiverSdkManager().sendVideoData(bytes2,bytes2.length,bufferInfo.presentationTimeUs,iskey);
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                }

                if (System.currentTimeMillis() - encoderTime2 > 2000) {
                    Bundle params = new Bundle();
                    params.putInt(MediaFormat.KEY_PREPEND_HEADER_TO_SYNC_FRAMES, 0);
                    mediaCodec.setParameters(params);
                    encoderTime2 = System.currentTimeMillis();
                    Log.e(TAG, "request I");
                }
            }
            mediaCodec.stop();
            mediaCodec.release();
        }).start();


    }
    private long encoderTime2 = 0;

    public Handler getHandler() {
        return handler;
    }

    public Handler handler;

    public void stop() {
        isStart = false;
        windowSurface.release();
        eglCore.release();

    }

}
