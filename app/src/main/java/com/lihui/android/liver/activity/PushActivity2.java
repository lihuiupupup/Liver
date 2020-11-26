package com.lihui.android.liver.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lihui.android.liver.R;
import com.lihui.android.liver.adapter.FilterAdapter;
import com.lihui.android.liver.adapter.FilterBean;
import com.lihui.android.liver.audio.AudioCapture;
import com.lihui.android.liver.audio.AudioEncoderThread;
import com.lihui.android.liver.filter.BaseFilter;
import com.lihui.android.liver.filter.BeautyFilter;
import com.lihui.android.liver.filter.BlackWhiteFilter;
import com.lihui.android.liver.filter.CameraControl;
import com.lihui.android.liver.filter.CameraFilter;
import com.lihui.android.liver.video.EncoderThread;
import com.lihui.android.liver.filter.FrameBufferFilter;
import com.lihui.android.liver.filter.NoFilter;
import com.lihui.android.liver.filter.ToScreenFilter;
import com.lihui.android.liver.utils.SPUtil;
import com.lihui.android.liver.view.LoadView;
import com.lihui.android.liversdk.ConnectUrlCallback;
import com.lihui.android.liversdk.LiverSdkManager;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PushActivity2 extends BaseActivity implements GLSurfaceView.Renderer, View.OnClickListener, FilterAdapter.OnItemSelectListener, AudioCapture.OnPcmAvailableListener {

    private static final String TAG = "PushActivity2";
    private GLSurfaceView glSurfaceView;
    private CameraFilter cameraShader;
    private FrameBufferFilter frameBufferFilter;
    private FrameBufferFilter frameBufferFilter2;
    private BlackWhiteFilter blackWhiteFilter;
    private ToScreenFilter toScreenFilter;
    private NoFilter noFilter;
    private BeautyFilter beautyFilter;
    private BaseFilter currentSelectFilter;
    private ImageView ivClose;
    private EditText etUrl;
    private TextView tvStartPush;

    private SurfaceTexture surfaceTexture;

    public static  int PUSH_WIDTH = 1080;
    public static  int PUSH_HEIGHT = 1920;
    public static  int BIT_RATE = 1500000;

    private int previewW, previewH;
    private int surfaceW = PUSH_WIDTH;
    private int surfaceH = PUSH_HEIGHT;

    private EncoderThread encoderThread;

    private Handler encoderThreadHandler;

    private long startPushTimeStamp = 0;

    private RecyclerView recyclerView;

    private ImageView ivSwitchCamera;
    private ImageView ivFilter;

    private ArrayList<FilterBean> filterBeans = new ArrayList<>();

    private FilterAdapter filterAdapter;

    private Handler UIHandler = new Handler();

    private LinearLayoutManager linearLayoutManager;

    private boolean isCameraChanged = false;

    private LinearLayout linearLayout;
    private LoadView loadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push2);
        loadView = findViewById(R.id.loading);
        glSurfaceView = findViewById(R.id.glsv);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(this);
        ivSwitchCamera = findViewById(R.id.iv_switch_camera);
        ivSwitchCamera.setOnClickListener(this);
        ivFilter = findViewById(R.id.iv_filter);
        ivFilter.setOnClickListener(this);
        linearLayout = findViewById(R.id.ll_connect);

        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(this);
        etUrl = findViewById(R.id.et_url);
        tvStartPush = findViewById(R.id.tv_start_push);
        tvStartPush.setOnClickListener(this);
        glSurfaceView.setOnClickListener(this);
        recyclerView = findViewById(R.id.rv_filter);
        filterAdapter = new FilterAdapter(this);
        String url = SPUtil.getSp(this).getString(SPUtil.PUSH_URL, "");
        if (!url.equals("")) {
            etUrl.setText(url);
        }
        initFilterView();

        initAudio();
    }

    private AudioCapture audioCapture;
    private AudioEncoderThread audioEncoderThread;
    private void initAudio() {
        audioCapture = new AudioCapture();
        audioCapture.setOnPcmAvailableListener(this);
        audioEncoderThread = new AudioEncoderThread();
    }

    private void initFilterView() {
        linearLayoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        filterAdapter.setData(filterBeans);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(filterAdapter);
        filterAdapter.setOnItemSelectListener(this);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        previewW = glSurfaceView.getWidth();
        previewH = glSurfaceView.getHeight();
        cameraShader = new CameraFilter(textureId -> {
            surfaceTexture = new SurfaceTexture(textureId);
            return surfaceTexture;
        });
        frameBufferFilter = new FrameBufferFilter(cameraShader, surfaceW, surfaceH);
        int frameBufferTextureId = frameBufferFilter.getFrameBufferTextureId();

        blackWhiteFilter = new BlackWhiteFilter(frameBufferTextureId,surfaceW,surfaceH);
        noFilter = new NoFilter(frameBufferTextureId,surfaceW,surfaceH);
        beautyFilter = new BeautyFilter(frameBufferTextureId,surfaceW,surfaceH);

        filterBeans.add(new FilterBean("原画",R.mipmap.ic_girl,noFilter,true));
        filterBeans.add(new FilterBean("黑白",R.mipmap.ic_girl,blackWhiteFilter,false));
        filterBeans.add(new FilterBean("美颜",R.mipmap.ic_girl,beautyFilter,false));

        currentSelectFilter = filterBeans.get(0).getFilter();
        frameBufferFilter2 = new FrameBufferFilter(currentSelectFilter, surfaceW, surfaceH);
        int frameBufferTextureId2 = frameBufferFilter2.getFrameBufferTextureId();

        toScreenFilter = new ToScreenFilter(frameBufferTextureId2);
        toScreenFilter.setWH(previewW, previewH);
        surfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
            glSurfaceView.requestRender();
        });
        encoderThread = new EncoderThread(EGL14.eglGetCurrentContext(), surfaceW, surfaceH, frameBufferTextureId2);
        encoderThreadHandler = encoderThread.getHandler();

        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                filterAdapter.notifyDataSetChanged();
            }
        });

        CameraControl.getCameraControl().setPreviewTexture(surfaceTexture);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraControl.getCameraControl().stop();
        encoderThread.stop();
        audioEncoderThread.stop();
        audioCapture.stop();
        surfaceTexture.release();
        LiverSdkManager.getLiverSdkManager().stopPush();

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
        }
        if (isCameraChanged) {
            cameraShader.init();
            isCameraChanged = false;
        }
        frameBufferFilter.draw();
        frameBufferFilter2.draw();
        toScreenFilter.draw();
        if (encoderThreadHandler != null && isStartPush) {
            Message obtain = Message.obtain();
            long current = System.nanoTime();
            long pts = current - startPushTimeStamp;
            obtain.obj = pts;
            encoderThreadHandler.sendMessage(obtain);
        }

    }

    private volatile boolean isStartPush = false;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_switch_camera:
                CameraControl.getCameraControl().switchCamera();
                isCameraChanged = true;
                break;
            case R.id.iv_filter:
                toggleFilterView();
                break;
            case R.id.glsv:
                onSurfaceClick();
                break;
            case R.id.iv_close:
                finish();
                break;
            case R.id.tv_start_push:
                tryFirePush();
                break;


        }
    }

    private String pushUrl ;
    private void tryFirePush() {
        pushUrl = etUrl.getText().toString().trim();
        //pushUrl =  "rtmp://192.168.0.105:1935/live/room2";
        //pushUrl = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_43632924_27814921&key=042a9df24d21044023bee14522bb6b77&schedule=rtmp";
        if (pushUrl.startsWith("rtmp://")) {
            firePush();
        } else {
            Toast.makeText(this,"地址不支持",Toast.LENGTH_SHORT).show();
        }
    }

    private void firePush() {

        loadView.toggleLoading();
        loadView.setTip("连接推流服务器中...");
        LiverSdkManager.getLiverSdkManager().startPush(pushUrl, result -> UIHandler.post(() -> dealConnectResult(result)));

    }

    private void dealConnectResult(int result) {
        if (result == 0) {
            loadView.setTip("正在推流中...");
            isStartPush = true;
            startPushTimeStamp = System.nanoTime();
            encoderThread.start();
            audioEncoderThread.start();
            audioCapture.start();
            hideConnectView();
            savePushUrl();
        } else {
            loadView.setTip("连接推流服务器失败...");
            Toast.makeText(this,"连接推流服务器失败...",Toast.LENGTH_SHORT).show();
        }
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadView.toggleLoading();
            }
        },1000);
    }

    private void savePushUrl() {
        SPUtil.getSp(this).edit().putString(SPUtil.PUSH_URL,pushUrl).apply();
    }

    private void hideConnectView() {
        linearLayout.setVisibility(View.GONE);
    }

    private void onSurfaceClick() {
        if(recyclerView.getVisibility() == View.VISIBLE) {
            toggleFilterView();
        }

    }

    private void toggleFilterView() {
        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelect(int pos) {
        frameBufferFilter2.setBaseFilter(filterBeans.get(pos).getFilter());
    }

    @Override
    public void onPcmData(byte[] data) {

        long pts = System.nanoTime() / 1000 - startPushTimeStamp / 1000;
        audioEncoderThread.putPcm(data,pts);
    }
}