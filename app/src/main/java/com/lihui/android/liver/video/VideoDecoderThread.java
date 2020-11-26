package com.lihui.android.liver.video;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;

import com.lihui.android.liver.MyApplication;
import com.lihui.android.liver.utils.YuvToRgb;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecoderThread {

    private static final String TAG = "VideoDecoderThread";
    private MediaCodec vDecodeCodec;

    private SurfaceView outPutSurface;

    private String mimeType = MediaFormat.MIMETYPE_VIDEO_AVC;

    public VideoDecoderThread(SurfaceView surface) {
        outPutSurface = surface;
    }

    private int lastWidth = 0;
    private int lastHeight = 0;


    public void configure(int width, int height, byte[] sps, byte[] pps) {
        if (lastWidth != width || lastHeight != height) {
            lastWidth = width;
            lastHeight = height;
            stop();
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
            mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));

            //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            try {
                vDecodeCodec = MediaCodec.createDecoderByType(mimeType);

                vDecodeCodec.configure(mediaFormat, outPutSurface.getHolder().getSurface(), null, 0);
                vDecodeCodec.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private volatile boolean isVideoDecoding = false;
    Thread thread;

    public void start() {
        if (isVideoDecoding) {
            return;
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isVideoDecoding = true;
                while (isVideoDecoding && !Thread.interrupted()) {

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int indexOut = vDecodeCodec.dequeueOutputBuffer(bufferInfo, 0);
                    if (indexOut >= 0) {
                       // Log.e(TAG, "format " + vDecodeCodec.getOutputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT));
                        Log.e(TAG, "yuv time stamp " + bufferInfo.presentationTimeUs);
                        if (bufferInfo.presentationTimeUs - MyApplication.timeStamp > 50_000) {
                            SystemClock.sleep(10);
                            vDecodeCodec.releaseOutputBuffer(indexOut, true);
                        } else if (bufferInfo.presentationTimeUs - MyApplication.timeStamp < 50_000) {
                            vDecodeCodec.releaseOutputBuffer(indexOut, true);
                        } else {
                              vDecodeCodec.releaseOutputBuffer(indexOut, true);
                        }
                        //vDecodeCodec.releaseOutputBuffer(indexOut, true);
                    }

                }
                vDecodeCodec.release();
                vDecodeCodec = null;
            }
        });
        thread.start();

    }

    public void putH264(byte[] h264, long pts) {

        if (isVideoDecoding) {
            try
            {
                int index = vDecodeCodec.dequeueInputBuffer(-1);

                if (index >= 0) {
                    ByteBuffer byteBuffer = vDecodeCodec.getInputBuffers()[index];
                    byteBuffer.clear();
                    byteBuffer.put(h264);
                    vDecodeCodec.queueInputBuffer(index, 0, h264.length, pts * 1000, 0);
                }
            }catch (IllegalStateException e) {

            }

        }

    }

    public void stop() {
        isVideoDecoding = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

}
