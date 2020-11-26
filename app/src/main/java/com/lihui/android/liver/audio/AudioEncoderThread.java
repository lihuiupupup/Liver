package com.lihui.android.liver.audio;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.lihui.android.liversdk.LiverSdkManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioEncoderThread {

    private static final String TAG = "AudioEncoderThread";
    private MediaCodec audioCodec;

    private String audioMime = MediaFormat.MIMETYPE_AUDIO_AAC;

    private int sampleRateInHz = 44100;

    private int channelCount = 2;


    public AudioEncoderThread() {
        init();
    }

    private void init() {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(audioMime, sampleRateInHz, channelCount);
        try {
            audioCodec = MediaCodec.createEncoderByType(audioMime);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT);
            audioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            audioCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private volatile boolean isEncoder = false;

    public void start() {
        if (isEncoder) {
            return;
        }
        if (audioCodec == null) {
            init();
        }
        isEncoder = true;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isEncoder) {
                    int i = audioCodec.dequeueOutputBuffer(bufferInfo, 100000);
                    if (i == -2) {
                        ByteBuffer ainfo = audioCodec.getOutputFormat().getByteBuffer("csd-0");
                        byte[] ainfob = new byte[ainfo.remaining()];
                        ainfo.get(ainfob, 0, ainfob.length);
                        LiverSdkManager.getLiverSdkManager().saveAudioHeader(ainfob,ainfob.length);
                    }
                    if (i >= 0) {
                        ByteBuffer outputBuffer = audioCodec.getOutputBuffers()[i];
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.size);


                        byte[] bytes = new byte[7 + bufferInfo.size];

                        outputBuffer.get(bytes, 7, bufferInfo.size);
                        addADTStoPacket(bytes, bytes.length);
                        if (bytes.length > 0) {
                            LiverSdkManager.getLiverSdkManager().sendAudioData(bytes,bytes.length,bufferInfo.presentationTimeUs);
                        }
                        Log.e(TAG,"aac data size" + bytes.length);
                        audioCodec.releaseOutputBuffer(i, false);
                    }
                }

                audioCodec.release();
                audioCodec = null;
            }
        }).start();

    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public void putPcm(byte[] data, long pts) {

        if (isEncoder) {
            int inputBuffer = audioCodec.dequeueInputBuffer(10000);
            if (inputBuffer >= 0) {
                ByteBuffer byteBuffer = audioCodec.getInputBuffers()[inputBuffer];
                byteBuffer.put(data);
                audioCodec.queueInputBuffer(inputBuffer, 0, data.length, pts, 0);
            }
        }
    }

    public void stop() {
        isEncoder = false;
    }
}
