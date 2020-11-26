package com.lihui.android.liver.filter;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ToScreenFilter extends BaseFilter {
    private static final String TAG = "";
    private int mProgram;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBuffer;
    private final float[] rotateMix = {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
    };

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 a_texturecoord;" +
                    "varying vec2 v_texturecoord;" +
                    "uniform mat4 rotateMatrix;" +
                    "void main() {" +
                    "  gl_Position = rotateMatrix * vPosition;" +
                    "v_texturecoord = a_texturecoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 v_texturecoord;" +
                    "uniform sampler2D u_samplerTexture;" +
                    "void main() {" +
                    "vec4 tmpColor = texture2D(u_samplerTexture,v_texturecoord);" +
                    "gl_FragColor = tmpColor;" +

                    "}";

    float triangleCoords[] = {
            -1f, 1f, 0.0f, // top left
            -1f, -1f, 0.0f, // bottom left
            1f, -1f, 0.0f,// bottom right
            1f, 1f, 0.0f  // top right
    };
    float textureCoords[] = {
            0f, 0f,  // top left
            0f, 1f, // bottom left
            1f, 1f, // bottom right
            1f, 0f // top right
    };
    short index[] = {
            0, 1, 2, 0, 2, 3
    };

    private int width;
    private int height;
    public void setWH(int w,int h) {
        width =w;
        height = h;
    }
    @Override
    public void init() {
        GLES20.glClearColor(0, 0, 0, 0);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4).order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(textureCoords.length * 4).order(ByteOrder.nativeOrder());

        textureBuffer = byteBuffer2.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);


        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);


        int vertexShader = EGLUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragShader = EGLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = EGLUtil.createProgram(vertexShader,fragShader);

        vertexIndex = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(vertexIndex);
        GLES20.glVertexAttribPointer(vertexIndex, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);

        textureIndex = GLES20.glGetAttribLocation(mProgram, "a_texturecoord");
        GLES20.glEnableVertexAttribArray(textureIndex);
        GLES20.glVertexAttribPointer(textureIndex, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, textureBuffer);

        //Matrix.scaleM(rotateMix,0,-1,0,0);
    }
    int vertexIndex;
    int textureIndex;
    @Override
    public void draw() {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glUseProgram(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glViewport(0,0,width,height);
        int rotateIndex = GLES20.glGetUniformLocation(mProgram,"rotateMatrix");
        GLES20.glUniformMatrix4fv(rotateIndex,1,false,rotateMix,0);

        int textureUniformIndex = GLES20.glGetUniformLocation(mProgram, "u_samplerTexture");

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

    private int textureId;

    public ToScreenFilter(int textureID) {
        this.textureId = textureID;
        init();
    }
}
