package com.lihui.android.liver.filter;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BlackWhiteFilter extends BaseFilter {

    private static final String TAG = "BlackWhiteFilter";
    private int mProgram;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer changeColorBuffer;
    private ShortBuffer indexBuffer;
    private final float[] rotateMix = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
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
                    "void modifyColor(vec4 color){" +
                    "    color.r=max(min(color.r,1.0),0.0);" +
                    "    color.g=max(min(color.g,1.0),0.0);" +
                    "    color.b=max(min(color.b,1.0),0.0);" +
                    "    color.a=max(min(color.a,1.0),0.0);" +
                    "}" +
                    "varying vec2 v_texturecoord;" +
                    "uniform sampler2D u_samplerTexture;" +
                    "uniform int filterType;" +
                    "uniform vec3 vChangeColor;" +
                    "void main() {" +
                    "vec4 tmpColor = texture2D(u_samplerTexture,v_texturecoord);" +
                    "if(filterType == 1) {" +
                    "    float average = (tmpColor.r + tmpColor.g + tmpColor.b) /3.0;" +
                    "    tmpColor.r = average;" +
                    "   tmpColor.g = average;" +
                    "   tmpColor.b = average;" +
                    "}" +
                    "else if(filterType == 2) {" +
                    "vec4 deltaColor=tmpColor+vec4(vChangeColor,0.0);" +
                    "modifyColor(deltaColor);" +
                    "tmpColor=deltaColor;" +
                    "}" +
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

    private float[] changeArray = new float[]{0.0f, 0.0f, 1.0f};

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

        ByteBuffer cg = ByteBuffer.allocateDirect(changeArray.length * 4).order(ByteOrder.nativeOrder());

        changeColorBuffer = cg.asFloatBuffer();
        changeColorBuffer.put(changeArray);
        changeColorBuffer.position(0);

        int vertexShader = EGLUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragShader = EGLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = EGLUtil.createProgram(vertexShader, fragShader);

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

    }

    int vertexIndex;
    int textureIndex;

    @Override
    public void draw() {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        int rotateIndex = GLES20.glGetUniformLocation(mProgram, "rotateMatrix");
        GLES20.glUniformMatrix4fv(rotateIndex, 1, false, rotateMix, 0);

        int filterTypeIndex = GLES20.glGetUniformLocation(mProgram, "filterType");
        GLES20.glUniform1i(filterTypeIndex, 1);

        int changeIndex = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
        GLES20.glUniform3fv(changeIndex, 1, changeArray, 0);

        int textureUniformIndex = GLES20.glGetUniformLocation(mProgram, "u_samplerTexture");
        GLES20.glUniform1i(textureUniformIndex, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    }

    @Override
    public void release() {
        GLES20.glDisableVertexAttribArray(vertexIndex);
        GLES20.glDisableVertexAttribArray(textureIndex);
        changeColorBuffer.clear();
        vertexBuffer.clear();
        textureBuffer.clear();
    }

    private int textureId;

    public BlackWhiteFilter(int textureID,int width,int height) {
        this.textureId = textureID;
        init();
    }
}
