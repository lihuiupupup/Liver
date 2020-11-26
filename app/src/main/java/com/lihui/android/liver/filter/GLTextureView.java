package com.lihui.android.liver.filter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private GLThread glThread;

    public GLTextureView(Context context) {
        super(context);
        init();
    }


    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        glThread = new GLThread(surface);
        glThread.setOnEglListener(onEglListener);
        new Thread(glThread).start();
    }

    private GLThread.OnEglListener onEglListener;

    public void setOnEglListener(GLThread.OnEglListener listener) {
        onEglListener = listener;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        glThread.setSize(width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        glThread.exit();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void requestRender() {
        glThread.requestRender();
    }
}
