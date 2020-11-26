package com.lihui.android.liver.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lihui.android.liver.R;
import com.lihui.android.liver.audio.AudioDecoderThread;
import com.lihui.android.liver.utils.SPUtil;
import com.lihui.android.liver.video.VideoDecoderThread;
import com.lihui.android.liver.view.LoadView;
import com.lihui.android.liversdk.ConnectUrlCallback;
import com.lihui.android.liversdk.LiverNativeCallback;
import com.lihui.android.liversdk.LiverSdkManager;

import java.lang.ref.WeakReference;

public class PullActivity extends BaseActivity implements View.OnClickListener, LiverNativeCallback, SurfaceHolder.Callback {

    private ImageView ivClose;
    private LinearLayout llConnect;
    private LoadView loadView;

    private EditText etUrl;
    private TextView tvStartPull;

    private SurfaceView surfaceView;

    private VideoDecoderThread videoDecoderThread;

    private AudioDecoderThread audioDecoderThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull);

        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(this);

        llConnect = findViewById(R.id.ll_connect);
        loadView = findViewById(R.id.loading);

        etUrl = findViewById(R.id.et_url);
        tvStartPull = findViewById(R.id.tv_start_pull);
        tvStartPull.setOnClickListener(this);

        surfaceView = findViewById(R.id.sv);
        videoDecoderThread = new VideoDecoderThread(surfaceView);
        surfaceView.getHolder().addCallback(this);

        String url = SPUtil.getSp(this).getString(SPUtil.PULL_URL, "");
        if (!url.equals("")) {
            etUrl.setText(url);
        }

        audioDecoderThread = new AudioDecoderThread();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_close) {
            finish();
        } else if (v.getId() == R.id.tv_start_pull) {

            tryFirePush();
        }
    }

    private String pullUrl ;
    private boolean isStartPull = false;
    private void tryFirePush() {
        pullUrl = etUrl.getText().toString().trim();
        //pushUrl =  "rtmp://192.168.0.105:1935/live/room2";
        //pushUrl = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_43632924_27814921&key=042a9df24d21044023bee14522bb6b77&schedule=rtmp";
        if (pullUrl.startsWith("rtmp://")) {
            firePush();
        } else {
            Toast.makeText(this,"地址不支持",Toast.LENGTH_SHORT).show();
        }
    }
    private Handler UIHandler = new Handler();
    private void firePush() {

        toggleLoading();
        loadView.setTip("正在连接拉流服务器...");
        hideConnectView();

        int i = LiverSdkManager.getLiverSdkManager().startPull(pullUrl, new WeakReference<LiverNativeCallback>(this).get(), new ConnectUrlCallback() {
            @Override
            public void onConnectResult(int result) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dealConnectResult(result);
                    }
                });
            }
        });

    }

    private void dealConnectResult(int result) {
        if (result == 0) {
            loadView.setTip("正在拉去视频流...");
            isStartPull = true;
            savePushUrl();
        } else {
            hideConnectView();
            Toast.makeText(this,"连接拉流服务器失败...",Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLoading() {
        if (loadView.getVisibility() == View.VISIBLE) {
            loadView.setVisibility(View.GONE);

        } else {
            loadView.setVisibility(View.VISIBLE);
        }
    }

    private void savePushUrl() {
        SPUtil.getSp(this).edit().putString(SPUtil.PULL_URL,pullUrl).apply();
    }

    private void hideConnectView() {
        if (llConnect.getVisibility() == View.VISIBLE) {
            llConnect.setVisibility(View.GONE);
        } else {
            llConnect.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVideoData(byte[] vData, long pts, boolean isKeyFrame) {
        if (isKeyFrame && loadView.getVisibility() == View.VISIBLE) {
            loadView.post(new Runnable() {
                @Override
                public void run() {
                    toggleLoading();
                }
            });
        }
        videoDecoderThread.putH264(vData,pts);
    }

    @Override
    public void onAudioData(byte[] aData, long pts, int sr, int sb, int c) {

        audioDecoderThread.putAAC(aData,(int)pts,false);

    }

    @Override
    public void onAudioHeader(byte[] data, int len,int sr,int sb,int c) {
        audioDecoderThread.configure(data,sr,sb,c);
        audioDecoderThread.start();
    }

    @Override
    public void onSpsPps(int width, int height, byte[] sps, byte[] pps, int spsLen, int ppsLen) {

        videoDecoderThread.configure(width,height,sps,pps);
        videoDecoderThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiverSdkManager.getLiverSdkManager().stopPull();
        audioDecoderThread.stop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        videoDecoderThread.stop();
    }
}