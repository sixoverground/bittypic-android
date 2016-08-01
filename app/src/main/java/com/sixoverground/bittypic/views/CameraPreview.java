package com.sixoverground.bittypic.views;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
  private static final String TAG = "CameraPreview";

  private SurfaceHolder mHolder;
  private Camera mCamera;
  private Activity mActivity;

  public CameraPreview(Activity activity, Camera camera) {
    super(activity);

    mActivity = activity;

    mCamera = camera;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.addCallback(this);
    // deprecated setting, but required on Android versions prior to 3.0
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    // The Surface has been created, now tell the camera where to draw the preview.
    try {
      mCamera.setPreviewDisplay(holder);
      mCamera.startPreview();
    } catch (IOException e) {
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // empty. Take care of releasing the Camera preview in your activity.
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.

    if (mHolder.getSurface() == null){
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try {
      mCamera.stopPreview();
    } catch (Exception e){
      // ignore: tried to stop a non-existent preview
    }

    // set preview size and make any resize, rotate or
    // reformatting changes here

    Camera.Parameters parameters = mCamera.getParameters();

    List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
    Camera.Size previewSize = previewSizes.get(0);
    for (Camera.Size size : previewSizes) {
      if (size.width < previewSize.width && size.width >= 612 && size.height < previewSize.height && size.height >= 612) {
        previewSize = size;
      }
    }
    parameters.setPreviewSize(previewSize.width, previewSize.height);

    List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
    Camera.Size pictureSize = pictureSizes.get(0);
    for (Camera.Size size : pictureSizes) {
      if (size.width < pictureSize.width && size.width >= 612 && size.height < pictureSize.height && size.height >= 612) {
        pictureSize = size;
      }
    }
    parameters.setPictureSize(pictureSize.width, pictureSize.height);

    mCamera.setParameters(parameters);

    setCameraDisplayOrientation(mActivity, 0, mCamera);

    // start preview with new settings
    try {
      mCamera.setPreviewDisplay(mHolder);
      mCamera.startPreview();

    } catch (Exception e){
    }
  }

  public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
    android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
    android.hardware.Camera.getCameraInfo(cameraId, info);
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0: degrees = 0; break;
      case Surface.ROTATION_90: degrees = 90; break;
      case Surface.ROTATION_180: degrees = 180; break;
      case Surface.ROTATION_270: degrees = 270; break;
    }

    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360;  // compensate the mirror
    } else {  // back-facing
      result = (info.orientation - degrees + 360) % 360;
    }
    camera.setDisplayOrientation(result);

    Camera.Parameters parameters = camera.getParameters();
    parameters.setRotation(result);
    camera.setParameters(parameters);

  }

}
