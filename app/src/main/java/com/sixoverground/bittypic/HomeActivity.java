package com.sixoverground.bittypic;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sixoverground.bittypic.adapters.PhotoRecyclerViewAdapter;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.Photo;
import com.sixoverground.bittypic.services.BittypicService;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class HomeActivity extends AppCompatActivity {

  private static final String TAG = "HomeActivity";
  private static final int REQUEST_CAMERA = 1;

  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private RecyclerView mRecyclerView;
  private BittypicModel.BittypicPhotoFeedListener mPhotoFeedListener;
  private PhotoRecyclerViewAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//            .setAction("Action", null).show();
        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_CAMERA);
      }
    });

    mAuth = FirebaseAuth.getInstance();
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          // User is signed in
        } else {
          // User is signed out
          BittypicModel.getInstance().logout();
          Intent intent = new Intent(HomeActivity.this, FacebookLoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);
          finish();
        }
        // ...
      }
    };

    mRecyclerView = (RecyclerView) findViewById(R.id.photo_list);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new PhotoRecyclerViewAdapter(new PhotoRecyclerViewAdapter.OnPhotoClickListener() {
      @Override
      public void onPhotoClick(Photo item) {

      }

      @Override
      public void onPhotoLikeClick(Photo item) {
        BittypicService.getInstance().likePhoto(item);
      }
    });
    mRecyclerView.setAdapter(mAdapter);

    mPhotoFeedListener = new BittypicModel.BittypicPhotoFeedListener() {
      @Override
      public void onPhotoAdded(Photo photo) {
//        mAdapter.addItem(photo);
        mAdapter.setItems(BittypicModel.getInstance().getPhotoFeed());
      }

      @Override
      public void onPhotoUpdated(Photo photo) {
        mAdapter.setItems(BittypicModel.getInstance().getPhotoFeed());
      }
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthListener);
    BittypicModel.getInstance().addPhotoFeedListener(mPhotoFeedListener);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAdapter.setItems(BittypicModel.getInstance().getPhotoFeed());
//    mAdapter.notifyDataSetChanged();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mAuthListener != null) {
      mAuth.removeAuthStateListener(mAuthListener);
    }
    if (mPhotoFeedListener != null) {
      BittypicModel.getInstance().removePhotoFeedListener(mPhotoFeedListener);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_home, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_logout) {
      logout();
      return true;
    } else if (id == R.id.action_find_friends) {
      findFriends();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CAMERA) {
      if (resultCode == RESULT_OK) {
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void logout() {
    LoginManager.getInstance().logOut();
    mAuth.signOut();
  }

  private void findFriends() {
    Intent intent = new Intent(this, FindFriendsActivity.class);
    startActivity(intent);
  }

}
