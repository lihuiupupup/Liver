package com.lihui.android.liver.filter;

import android.opengl.GLES20;
import android.util.Log;


public class FrameBufferFilter extends BaseFilter {

    private static final String TAG = "FrameBufferFilter";
    private int frameBufferTextureId;
    private int frameBufferId;

    private int width;
    private int height;

    private BaseFilter baseFilter;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public FrameBufferFilter(BaseFilter filter, int width, int height) {
        this.baseFilter = filter;
        this.width = width;
        this.height = height;
        init();
    }

    public void setBaseFilter(BaseFilter filter) {
        this.baseFilter = filter;
    }

    @Override
    public void init() {
        frameBufferTextureId = EGLUtil.createTexture(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,frameBufferTextureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        int[] framebuffer = new int[1];
        GLES20.glGenFramebuffers(1, framebuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,framebuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameBufferTextureId, 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)== GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "glFramebufferTexture2D error");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        frameBufferId = framebuffer[0];
    }


    public int getFrameBufferTextureId() {
        return frameBufferTextureId;
    }

    @Override
    public void draw() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,frameBufferId);
        GLES20.glViewport(0,0,width,height);
        baseFilter.draw();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }

    @Override
    public void release() {

    }
}
