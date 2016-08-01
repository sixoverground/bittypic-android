package com.sixoverground.bittypic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.sixoverground.bittypic.views.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class CameraActivity extends AppCompatActivity {

  public static final int MEDIA_TYPE_IMAGE = 1;

  private static final String TAG = "CameraActivity";
  private static final int REQUEST_EDIT_PHOTO = 1;

  private Camera mCamera;
  private CameraPreview mPreview;
  String mCurrentPhotoPath;

  private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

      File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
      if (pictureFile == null){
        return;
      }

      try {
        FileOutputStream fos = new FileOutputStream(pictureFile);
        fos.write(data);
        fos.close();

        Uri uri = Uri.fromFile(pictureFile);
        editPhoto(uri);

      } catch (FileNotFoundException e) {
      } catch (IOException e) {
      }

    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

    Button captureButton = (Button) findViewById(R.id.button_capture);
    captureButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mCamera != null) {
          mCamera.takePicture(null, null, mPicture);
        }
      }
    });

  }

  @Override
  protected void onResume() {
    super.onResume();

    // Create an instance of Camera
    mCamera = getCameraInstance();

    if (mCamera != null) {
      // Create our Preview view and set it as the content of our activity.
      mPreview = new CameraPreview(this, mCamera);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      preview.addView(mPreview);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseCamera();              // release the camera immediately on pause event
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_EDIT_PHOTO) {
      if (resultCode == RESULT_OK) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private boolean checkCameraHardware(Context context) {
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
      // this device has a camera
      return true;
    } else {
      // no camera on this device
      return false;
    }
  }

  public static Camera getCameraInstance(){
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }

  private void releaseCamera() {
    if (mCamera != null){
      mCamera.setPreviewCallback(null);
      mPreview.getHolder().removeCallback(mPreview);
      mCamera.release();        // release the camera for other applications
      mCamera = null;
    }
  }

  private static File getOutputMediaFile(int type){
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.
    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), "Bittypic");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
      if (! mediaStorageDir.mkdirs()){
        return null;
      }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE){
      mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "photo_"+ timeStamp + ".jpg");
    } else {
      return null;
    }

    return mediaFile;
  }

  private void editPhoto(Uri uri) {
    Intent intent = new Intent(CameraActivity.this, EditPhotoActivity.class);
    intent.putExtra(EditPhotoActivity.PHOTO, uri.toString());
    startActivityForResult(intent, REQUEST_EDIT_PHOTO);
  }

}
