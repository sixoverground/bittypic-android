package com.sixoverground.bittypic;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.User;
import com.sixoverground.bittypic.services.BittypicService;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private BittypicModel.BittypicAuthStateListener mBittypicAuthStateListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mAuth = FirebaseAuth.getInstance();
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          // User is signed in
          BittypicService.getInstance().getOrCreateCurrentUser(user);
        } else {
          // User is signed out
          Intent intent = new Intent(MainActivity.this, FacebookLoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);
          finish();
        }
        // ...
      }
    };

    mBittypicAuthStateListener = new BittypicModel.BittypicAuthStateListener() {
      @Override
      public void onUserLoggedIn(User user) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
      }
    };

  }

  @Override
  public void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthListener);
    BittypicModel.getInstance().addAuthStateListener(mBittypicAuthStateListener);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (mAuthListener != null) {
      mAuth.removeAuthStateListener(mAuthListener);
    }
    if (mBittypicAuthStateListener != null) {
      BittypicModel.getInstance().removeAuthStateListener(mBittypicAuthStateListener);
    }
  }

}
