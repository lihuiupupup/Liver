package com.lihui.android.liver.filter;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.util.Log;
import android.view.Surface;

import static com.lihui.android.liver.filter.EglCore.FLAG_RECORDABLE;

public class GLThread implements Runnable{

    private static final String TAG = "GLThread";
    private EglCore eglCore;

    private Surface surface;

    private SurfaceTexture surfaceTexture;

    private WindowSurface windowSurface;

    private int threadStatus = UNCREATED;
    private static final int UNCREATED = -1;
    private static final int CREATED = 0;

    private static final int RUNNING = 1;

    private static final int PAUSED = 2;

    private static final int RELEASE = 3;

    private EGLContext shareCtx;

    private int threadSignal = SIGNAL_DEFAULT;

    private static final int SIGNAL_DEFAULT = -1;
    private static final int SIGNAL_EXIT = 0;

    private static final int SIGNAL_PAUSE = 1;

    private static final int SIGNAL_RESUME = 2;

    private static final int SIGNAL_FRAME_AVAILABLE = 3;
    private static final int SIGNAL_SIZE = 4;

    private int width;

    private int height;

    private Object lock = new Object();

    public GLThread() {
        threadStatus = CREATED;
    }

    public GLThread(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
        threadStatus = CREATED;
    }

    public GLThread(Surface surface) {
        this.surface = surface;
        threadStatus = CREATED;
    }

    public void setShareCtx(EGLContext eglContext) {
        shareCtx = eglContext;

    }

    @Override
    public void run() {
        createEglEnv();
        if (onEglListener != null) {
            onEglListener.onEglEnvAvailable();
        }
        threadStatus = RUNNING;
        while (true) {
            if (threadSignal == SIGNAL_EXIT) {
                threadStatus = RELEASE;
                break;
            }
            if (threadSignal == SIGNAL_PAUSE) {
                threadStatus = PAUSED;
            }
            if (threadSignal == SIGNAL_RESUME) {
                threadStatus = RUNNING;
            }
            if (threadSignal == SIGNAL_SIZE) {
                if (onEglListener != null) {
                    onEglListener.onSizeChanged(width,height);
                }
            }
            if (threadSignal == SIGNAL_FRAME_AVAILABLE) {
                if (onEglListener != null) {
                    onEglListener.onDrawFrame();
                    dealFrame();
                }
            } else {
                waitThread();
            }

        }
        if (onEglListener != null) {
            onEglListener.onEglEvnDestroy();
        }
        releaseEgl();
    }

    private void releaseEgl() {
        if (windowSurface != null) {
            windowSurface.release();
        }
        if (eglCore != null) {
            eglCore.release();
        }
    }


    private void dealFrame() {
        if (windowSurface != null) {
            windowSurface.swapBuffers();
        }
        waitThread();
    }

    private void createEglEnv() {
        eglCore = new EglCore(shareCtx,FLAG_RECORDABLE);
        if (surface != null) {
            windowSurface = new WindowSurface(eglCore,surface,true);
        }

        if (surfaceTexture != null) {
            windowSurface = new WindowSurface(eglCore,surfaceTexture);
        }

        if (windowSurface != null) {
            windowSurface.makeCurrent();
        } else {
            Log.e(TAG,"no native window");
        }
    }

    public void exit() {
        threadSignal = SIGNAL_EXIT;
        notifyThread();
    }

    public void onPause() {
        threadSignal = SIGNAL_PAUSE;
        notifyThread();
    }

    public void onResume() {
        threadSignal = SIGNAL_RESUME;
        notifyThread();
    }

    public void requestRender() {
        if (threadStatus == PAUSED) {
            return;
        }
        threadSignal = SIGNAL_FRAME_AVAILABLE;
        notifyThread();
    }

    private void notifyThread() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void waitThread() {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSize(int width,int height) {
        this.width = width;
        this.height = height;
        threadSignal = SIGNAL_SIZE;
        notifyThread();
    }

    public int getThreadStatus() {
        return threadStatus;
    }
    public void setOnEglListener(OnEglListener l) {
        this.onEglListener = l;
    }

    private OnEglListener onEglListener;
    public interface OnEglListener {

        void onEglEnvAvailable();
        void onDrawFrame();

        void onEglEvnDestroy();

        void onSizeChanged(int width, int height);
    }
}
