package com.sixoverground.bittypic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sixoverground.bittypic.adapters.FriendRecyclerViewAdapter;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.User;
import com.sixoverground.bittypic.services.BittypicService;

import java.util.ArrayList;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class FindFriendsActivity extends AppCompatActivity {

  private static final String TAG = "FindFriendsActivity";

  private RecyclerView mRecyclerView;
  private FriendRecyclerViewAdapter mAdapter;
  private BittypicModel.BittypicUserListener mUserListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_friends);

    mRecyclerView = (RecyclerView) findViewById(R.id.photo_list);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new FriendRecyclerViewAdapter(new FriendRecyclerViewAdapter.OnFriendClickListener() {
      @Override
      public void onFriendClick(User item) {

      }

      @Override
      public void onFriendFollowClick(User item) {
        BittypicService.getInstance().followUser(item);
      }
    });
    mRecyclerView.setAdapter(mAdapter);

    mUserListener = new BittypicModel.BittypicUserListener() {
      @Override
      public void onUserUpdated(User user) {
        ArrayList<User> friends = BittypicModel.getInstance().getFriends();
        mAdapter.setItems(friends);
      }
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    BittypicModel.getInstance().addUserListener(mUserListener);
  }

  @Override
  protected void onResume() {
    super.onResume();
    ArrayList<User> friends = BittypicModel.getInstance().getFriends();
    mAdapter.setItems(friends);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mUserListener != null) {
      BittypicModel.getInstance().removeUserListener(mUserListener);
    }
  }
}
