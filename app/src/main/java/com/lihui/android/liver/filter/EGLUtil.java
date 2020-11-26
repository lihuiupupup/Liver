package com.lihui.android.liver.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.opengles.GL10;

public class EGLUtil {

    private static final String TAG = "ShadeUtil";

    public static int loadShader(int shaderType, String vertexShaderCode) {

        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, vertexShaderCode);
        GLES20.glCompileShader(shader);
        int status[] = new int[1];
        
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String ss = GLES20.glGetShaderInfoLog(shader);
            Log.e(TAG, "create shader success" + ss);
            GLES20.glDeleteShader(shader);
            shader = 0;
        } else {
            Log.e(TAG, "create shader success");
        }

        return shader;
    }

    public static int createTexture(int target) {

        int[] textureObjectId = new int[1];
        GLES20.glGenTextures(1, textureObjectId, 0);
        //绑定纹理
        GLES20.glBindTexture(target, textureObjectId[0]);
        //设置放大缩小。设置边缘测量
        GLES20.glTexParameterf(target,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(target,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(target,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return textureObjectId[0];
    }

    public static int  createProgram(int vertexShader, int fragShader) {
        int mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragShader);

        GLES20.glLinkProgram(mProgram);

        int status[] = new int[1];
        //获取链接结果
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
        if (mProgram == 0) {
            Log.e(TAG, "create program fail");
        } else {
            Log.e(TAG, "create program success");
        }

        return mProgram;

    }

    public static String readTextFromRawResource(final Context applicationContext,
                                                  final int resourceId) {
        final InputStream inputStream =
                applicationContext.getResources().openRawResource(resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String nextLine;
        final StringBuilder body = new StringBuilder();
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }

        return body.toString();
    }

}
