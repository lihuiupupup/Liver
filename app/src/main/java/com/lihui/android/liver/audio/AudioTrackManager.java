package com.lihui.android.liver.audio;

import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.LinkedBlockingDeque;

class AudioTrackManager {

    private AudioTrack audioTrack;


    public void init(int mSampleRateInHz, int mChannelConfig, int mAudioFormat) {


        int mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);//计算最小缓冲区
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
        int mStreamType = AudioManager.STREAM_MUSIC;
        int mMode = AudioTrack.MODE_STREAM;
        audioTrack = new AudioTrack(mStreamType, mSampleRateInHz,mChannelConfig,
                mAudioFormat,mMinBufferSize,mMode);
        audioTrack.play();
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning)
                if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED
                        && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    byte[] data = new byte[0];
                    try {
                        data = concurrentLinkedQueue.takeFirst();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    audioTrack.write(data,0,data.length);
                }
            }
        }).start();
    }

    boolean isRunning = false;
    private LinkedBlockingDeque<byte[]> concurrentLinkedQueue = new LinkedBlockingDeque<>();

    public void putPcm(byte[] data) {

        concurrentLinkedQueue.add(data);

    }

    public void stop() {
        if (audioTrack != null) {
            isRunning = false;
            concurrentLinkedQueue.clear();
            audioTrack.pause();
            audioTrack.stop();
            audioTrack.release();
        }

    }


}
