package com.lihui.android.liver.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioCapture {

    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;

    private volatile boolean isCapture = false;
    private int minBufferSize;

    public void start() {
        if (isCapture) {
            return;
        }
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz, channelConfig,
                audioFormat, minBufferSize);
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                isCapture = true;
                minBufferSize /= 2;
                byte[] buffer = new byte[minBufferSize];
                while (isCapture) {
                    int read = audioRecord.read(buffer, 0, minBufferSize);
                    if (onPcmAvailableListener != null) {
                        onPcmAvailableListener.onPcmData(buffer);
                    }
                }
                audioRecord.release();
            }
        }).start();

    }

    public void stop() {
        isCapture = false;
    }

    private OnPcmAvailableListener onPcmAvailableListener;

    public void setOnPcmAvailableListener(OnPcmAvailableListener onPcmAvailableListener) {
        this.onPcmAvailableListener = onPcmAvailableListener;
    }

    public interface OnPcmAvailableListener {
        void onPcmData(byte[] data);
    }
}
