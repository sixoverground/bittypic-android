package com.sixoverground.bittypic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sixoverground.bittypic.models.Photo;
import com.sixoverground.bittypic.services.BittypicService;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class EditPhotoActivity extends AppCompatActivity {

  public static final String PHOTO = "photo";

  private static final String TAG = "EditPhotoActivity";

  private String mPhoto;
  private Uri mPhotoUri;
  private ImageView mPhotoImage;
  private EditText mCaptionText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_photo);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      Bundle extras = getIntent().getExtras();
      if (extras != null) {
        mPhoto = extras.getString(PHOTO);
      }
    } else {
      mPhoto = savedInstanceState.getString(PHOTO);
    }

    mPhotoImage = (ImageView) findViewById(R.id.image_photo);
    mCaptionText = (EditText) findViewById(R.id.text_caption);

    mPhotoUri = Uri.parse(mPhoto);
    Context context = mPhotoImage.getContext();
    Glide.with(context).clear(mPhotoImage);
    RequestOptions options = new RequestOptions()
        .centerCrop();
    Glide
        .with(context)
        .load(mPhotoUri)
        .apply(options)
        .into(mPhotoImage);
  }

  @Override
  protected void onDestroy() {
    mPhotoImage.setImageURI(null);
    mPhotoUri = null;
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_edit_photo, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_publish) {
      uploadPhoto();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void uploadPhoto() {
    Uri photoUri = Uri.parse(mPhoto);
    byte[] data = null;
    try {
      InputStream inputStream = getContentResolver().openInputStream(photoUri);
      data = getBytes(inputStream);
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
    }

    if (data != null) {
      FirebaseStorage storage = FirebaseStorage.getInstance();
      StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.firebase_bucket));
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
      String fileName = "photo_"+ timeStamp + ".jpg";
      final StorageReference photoRef = storageRef.child("photos/" + fileName);

      UploadTask uploadTask = photoRef.putBytes(data);

//      uploadTask.addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception exception) {
//          // Handle unsuccessful uploads
//        }
//      }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//        @Override
//        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//          // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//          Uri downloadUrl = taskSnapshot.getDownloadUrl();
//          savePhoto(downloadUrl);
//        }
//      });

      Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
        @Override
        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          // Continue with the task to get the download URL
          return photoRef.getDownloadUrl();
        }
      }).addOnCompleteListener(new OnCompleteListener<Uri>() {
        @Override
        public void onComplete(@NonNull Task<Uri> task) {
          if (task.isSuccessful()) {
            Uri downloadUri = task.getResult();
            savePhoto(downloadUri);
          } else {
            // Handle failures
            // ...
          }
        }
      });


    }

  }

  private byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }

  private void savePhoto(Uri downloadUrl) {
    String caption = mCaptionText.getText().toString();
    Photo photo = new Photo();
    photo.url = downloadUrl.toString();
    photo.caption = caption;
    BittypicService.getInstance().createPhoto(photo, new BittypicService.BitlyServiceListener() {
      @Override
      public void onBitlyServiceSuccess() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
      }

      @Override
      public void onBitlyServiceFailure() {
      }
    });
  }

}
