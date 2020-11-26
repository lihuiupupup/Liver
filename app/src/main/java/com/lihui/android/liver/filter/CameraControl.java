package com.lihui.android.liver.filter;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.lihui.android.liver.activity.PushActivity2;

import java.io.IOException;
import java.util.List;

public class CameraControl {


    private static CameraControl cameraControl = new CameraControl();
    private CameraControl() {

    }
    private Camera camera;
    private void openCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    private boolean isFrontCamera = true;

    public void switchCamera() {
        if (isFrontCamera) {
            switchCamera(false);
            isFrontCamera = false;
        } else {
            switchCamera(true);
            isFrontCamera = true;
        }
    }

    private void switchCamera(boolean isFrontCamera) {
        if (camera != null) {
            stop();

            if (isFrontCamera) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            resetParameters();
            try {
                camera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }

            camera.startPreview();

        }


    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public static CameraControl getCameraControl() {
        return cameraControl;
    }

    private SurfaceTexture surfaceTexture;
    public void setPreviewTexture(SurfaceTexture surface) {
        this.surfaceTexture = surface;
        if (camera == null) {
            openCamera();
        }
        resetParameters();
        try {
            camera.setPreviewTexture(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.startPreview();

    }

    private int width = PushActivity2.PUSH_HEIGHT;
    private int height = PushActivity2.PUSH_WIDTH;

    private void resetParameters() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(width,height);
            parameters.setPictureSize(width,height);
            camera.setDisplayOrientation(90);
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                parameters.setFocusMode(supportedFocusModes.get(0));
            }
            parameters.getSupportedPictureSizes();
            camera.setParameters(parameters);
        }
    }

    public void stop() {
        camera.stopPreview();
        camera.release();

        isFrontCamera = true;
        camera = null;
    }
}
