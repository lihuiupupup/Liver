package com.lihui.android.liver.filter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class CameraFilter extends BaseFilter {
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 tPosition;" +
                    "varying vec2 fPosition;" +
                    "uniform mat4 rotateMatrix;" +
                    "void main() {" +
                    "  gl_Position = rotateMatrix * vPosition;" +
                    "fPosition = tPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external:require\n" +
                    "precision mediump float;" +
                    "varying vec2 fPosition;" +
                    "uniform samplerExternalOES u_samplerTexture;" +
                    "void main() {" +
                    "gl_FragColor = texture2D(u_samplerTexture,fPosition);" +
                    "}";


    private final float[] vPositionData = {
            -1, -1, 0,
            -1, 1, 0,
            1, 1, 0,
            1, -1, 0
    };

    private final short index[] = {
            0, 1, 2, 0, 2, 3
    };

    private final float[] tPositionData = {
            0, 1,
            0, 0,
            1, 0,
            1, 1,
    };

    private float[] rotateMatrix = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    private SurfaceTexture surfaceTexture;
    private int program;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBuffer;


    public void loadVertexAndFrag() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vPositionData.length * 4).order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vPositionData);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(tPositionData.length * 4).order(ByteOrder.nativeOrder());

        textureBuffer = byteBuffer2.asFloatBuffer();
        textureBuffer.put(tPositionData);
        textureBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

    }

    private int vertexIndex;

    private int textureIndex;


    public void loadProgram() {

        int vertexShader = EGLUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragShader = EGLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        program = EGLUtil.createProgram(vertexShader, fragShader);

        if (CameraControl.getCameraControl().isFrontCamera()) {
            resetRotate();
            Matrix.rotateM(rotateMatrix, 0, 90f, 0, 0, 1);
        } else {
            resetRotate();
            Matrix.rotateM(rotateMatrix, 0, 270f, 0, 0, 1);
        }

        vertexIndex = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(vertexIndex);
        GLES20.glVertexAttribPointer(vertexIndex, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);

        textureIndex = GLES20.glGetAttribLocation(program, "tPosition");
        GLES20.glEnableVertexAttribArray(textureIndex);
        GLES20.glVertexAttribPointer(textureIndex, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, textureBuffer);
    }

    private void resetRotate() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    rotateMatrix[i * 4 + j] = 1;
                } else {
                    rotateMatrix[i * 4 + j] = 0;
                }
            }
        }
    }

    @Override
    public void init() {
        loadVertexAndFrag();
        loadProgram();
    }

    @Override
    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glUseProgram(program);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);

        int rotateIndex = GLES20.glGetUniformLocation(program, "rotateMatrix");
        GLES20.glUniformMatrix4fv(rotateIndex, 1, false, rotateMatrix, 0);


        int textureUniformIndex = GLES20.glGetUniformLocation(program, "u_samplerTexture");


        GLES20.glUniform1i(textureUniformIndex, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    }

    @Override
    public void release() {
        GLES20.glDisableVertexAttribArray(vertexIndex);
        GLES20.glDisableVertexAttribArray(textureIndex);
        vertexBuffer.clear();
        textureBuffer.clear();
    }


    private int cameraTextureId;

    public CameraFilter(CameraSurfaceTextureListener listener) {
        this.surfaceTextureListener = listener;
        cameraTextureId = EGLUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        surfaceTexture = surfaceTextureListener.createSurfaceTexture(cameraTextureId);
        init();
    }

    private CameraSurfaceTextureListener surfaceTextureListener;

    public interface CameraSurfaceTextureListener {
        SurfaceTexture createSurfaceTexture(int textureId);
    }

}
