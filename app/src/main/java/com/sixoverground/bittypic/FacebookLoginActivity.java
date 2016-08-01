package com.sixoverground.bittypic;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.User;
import com.sixoverground.bittypic.services.BittypicService;

import java.util.Arrays;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class FacebookLoginActivity extends AppCompatActivity {

  private static final String TAG = "FacebookLoginActivity";

  private CallbackManager mCallbackManager;
  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private BittypicModel.BittypicAuthStateListener mBittypicAuthStateListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_facebook_login);

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
        }
        // ...
      }
    };

    mCallbackManager = CallbackManager.Factory.create();
    LoginManager.getInstance().registerCallback(mCallbackManager,
        new FacebookCallback<LoginResult>() {
          @Override
          public void onSuccess(LoginResult loginResult) {
            // App code
            handleFacebookAccessToken(loginResult.getAccessToken());
          }

          @Override
          public void onCancel() {
            // App code
          }

          @Override
          public void onError(FacebookException exception) {
            // App code
            if (exception instanceof FacebookAuthorizationException) {
              if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
                login();
              }
            }
          }
        });

    Button loginButton = (Button) findViewById(R.id.button_login);
    loginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        login();
      }
    });

    mBittypicAuthStateListener = new BittypicModel.BittypicAuthStateListener() {
      @Override
      public void onUserLoggedIn(User user) {
        Intent intent = new Intent(FacebookLoginActivity.this, HomeActivity.class);
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mCallbackManager.onActivityResult(requestCode, resultCode, data);
  }

  private void handleFacebookAccessToken(AccessToken token) {

    AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {

            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
              Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",
                  Toast.LENGTH_SHORT).show();
            }

            // ...
          }
        });

  }

  private void login() {
    LoginManager.getInstance().logInWithReadPermissions(FacebookLoginActivity.this, Arrays.asList("public_profile", "email", "user_friends"));
  }

}
