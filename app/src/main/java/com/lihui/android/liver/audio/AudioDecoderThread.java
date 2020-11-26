package com.lihui.android.liver.audio;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

import com.lihui.android.liver.MyApplication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioDecoderThread {

    private static final String TAG = "AudioDecoderThread";
    private MediaCodec audioDecode;

    private int simpleRate;

    private int channelConfigs;

    private int audioFormat;

    private MediaCodec.BufferInfo bufferInfo;

    private AudioTrackManager audioTrackManager;

    private int mSr, mSb, mC;

    public AudioDecoderThread() {
        audioTrackManager = new AudioTrackManager();
    }

    public void configure(byte[] data, int sr, int sb, int c) {
        if (mSr != sr || mSb != sb || mC != c) {
            mSr = sr;
            mSb = sb;
            mC = c;
            try {
                stop();
                setParameter(sr, sb, c);
                audioTrackManager.init(simpleRate, channelConfigs, audioFormat);
                audioDecode = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, simpleRate, getCountByConfig(channelConfigs));
                mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);

                ByteBuffer csd_0 = ByteBuffer.wrap(data);
                mediaFormat.setByteBuffer("csd-0", csd_0);
                audioDecode.configure(mediaFormat, null, null, 0);
                audioDecode.start();
                bufferInfo = new MediaCodec.BufferInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private boolean isDecoder = false;
    private ConcurrentLinkedQueue<VideoData> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    public void start() {

        if (isDecoder) {
            return;
        }

        isDecoder = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //SystemClock.sleep(1000);
                while (isDecoder) {


                    int i = audioDecode.dequeueOutputBuffer(bufferInfo, 0);
                    if (i >= 0) {

                        ByteBuffer outputBuffer = audioDecode.getOutputBuffers()[i];

                        byte[] bytes = new byte[outputBuffer.remaining()];

                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.size);
                        outputBuffer.get(bytes, outputBuffer.position(), outputBuffer.limit());
                        MyApplication.timeStamp = bufferInfo.presentationTimeUs;
                        Log.e(TAG, "pcm time stamp " + bufferInfo.presentationTimeUs);
                        audioTrackManager.putPcm(bytes);
                        audioDecode.releaseOutputBuffer(i, false);

                    }


                }

                audioDecode.release();
                audioDecode = null;
            }
        }).start();


    }

    public void putAAC(byte[] aac, int pts, boolean isHeader) {
        if (isDecoder) {
            int index = audioDecode.dequeueInputBuffer(1000);
            if (index >= 0) {

                ByteBuffer inputBuffer = audioDecode.getInputBuffers()[index];
                inputBuffer.clear();
                inputBuffer.put(aac);
                if (isHeader) {
                    audioDecode.queueInputBuffer(index, 0, aac.length, pts * 1000, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                } else {
                    audioDecode.queueInputBuffer(index, 0, aac.length, pts * 1000, 0);
                }
            }
        }

    }

    public void stop() {
        isDecoder = false;
        audioTrackManager.stop();
    }

    public int getCountByConfig(int channelConfig) {
        int channelCount = 0;
        switch (channelConfig) {
            case AudioFormat.CHANNEL_OUT_MONO:
            case AudioFormat.CHANNEL_CONFIGURATION_MONO:
                channelCount = 1;
                break;
            case AudioFormat.CHANNEL_OUT_STEREO:
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
                channelCount = 2;
                break;

        }
        return channelCount;
    }

    public void setParameter(int sr, int sb, int c) {
        simpleRate = 44100;
        if (sr == 0) {
            simpleRate = 5500;
        } else if (sr == 1) {
            simpleRate = 11000;
        } else if (sr == 2) {
            simpleRate = 22000;
        } else if (sr == 3) {
            simpleRate = 44100;
        }
        if (sb == 0) {
            audioFormat = AudioFormat.ENCODING_PCM_8BIT;

        } else {
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        }
        if (c == 0) {
            channelConfigs = AudioFormat.CHANNEL_OUT_MONO;

        } else {
            channelConfigs = AudioFormat.CHANNEL_OUT_STEREO;
        }


    }
}
