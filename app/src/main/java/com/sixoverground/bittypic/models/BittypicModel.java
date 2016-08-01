package com.sixoverground.bittypic.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixoverground.bittypic.BittypicApplication;
import com.sixoverground.bittypic.R;
import com.sixoverground.bittypic.comparators.PhotoDateComparator;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class BittypicModel {

  private static final String TAG = "BittypicModel";

  private static BittypicModel sInstance;

  private ArrayList<BittypicAuthStateListener> mAuthStateListeners;
  private ArrayList<BittypicPhotoFeedListener> mPhotoFeedListeners;
  private ArrayList<BittypicUserListener> mUserListeners;
  private String mCurrentUserUid;
  private ArrayList<User> mUsers;
  private ArrayList<Photo> mPhotos;

  private BittypicModel() {
    mAuthStateListeners = new ArrayList<>();
    mPhotoFeedListeners = new ArrayList<>();
    mUserListeners = new ArrayList<>();
    mUsers = new ArrayList<>();
    mPhotos = new ArrayList<>();
  }

  public static synchronized BittypicModel getInstance() {
    if (sInstance == null) sInstance = new BittypicModel();
    return sInstance;
  }

  public void addAuthStateListener(BittypicAuthStateListener authStateListener) {
    mAuthStateListeners.add(authStateListener);
  }

  public void removeAuthStateListener(BittypicAuthStateListener authStateListener) {
    mAuthStateListeners.remove(authStateListener);
  }

  public void addPhotoFeedListener(BittypicPhotoFeedListener photoFeedListener) {
    mPhotoFeedListeners.add(photoFeedListener);
  }

  public void removePhotoFeedListener(BittypicPhotoFeedListener photoFeedListener) {
    mPhotoFeedListeners.remove(photoFeedListener);
  }

  public void addUserListener(BittypicUserListener userListener) {
    mUserListeners.add(userListener);
  }

  public void removeUserListener(BittypicUserListener userListener) {
    mUserListeners.remove(userListener);
  }

  public String getDeviceToken() {
    BittypicApplication app = BittypicApplication.getInstance();
    SharedPreferences sharedPref = app.getSharedPreferences(app.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    return sharedPref.getString(app.getString(R.string.preference_device_token), null);
  }

  public void setDeviceToken(String deviceToken) {
    BittypicApplication app = BittypicApplication.getInstance();
    SharedPreferences sharedPref = app.getSharedPreferences(app.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    sharedPref.edit().putString(app.getString(R.string.preference_device_token), deviceToken).apply();
  }

  public void login(User user) {
    mCurrentUserUid = user.uid;
    addOrUpdateUser(user);
    for (BittypicAuthStateListener listener : mAuthStateListeners) {
      listener.onUserLoggedIn(user);
    }
  }

  public void logout() {
    mCurrentUserUid = null;
  }

  public User getCurrentUser() {
    if (mCurrentUserUid != null) {
      return getUser(mCurrentUserUid);
    }
    return null;
  }

  public void addOrUpdateUser(User user) {
    User existingUser = getUser(user.uid);
    if (existingUser == null) {
      mUsers.add(user);
    } else {
      existingUser.displayName = user.displayName;
      existingUser.email = user.email;
      existingUser.photoUrl = user.photoUrl;
      existingUser.deviceToken = user.deviceToken;
      existingUser.facebookFriends = user.facebookFriends;
      existingUser.photos = user.photos;
      existingUser.likes = user.likes;
      existingUser.following = user.following;
      existingUser.followers = user.followers;
      for (BittypicUserListener listener : mUserListeners) {
        listener.onUserUpdated(existingUser);
      }
    }
  }

  public User getUser(String uid) {
    for (User user : mUsers) {
      if (user.uid.equals(uid)) {
        return user;
      }
    }
    return null;
  }

  public User getFacebookUser(String providerUid) {
    for (User user : mUsers) {
      if (user.providerUid.equals(providerUid)) {
        return user;
      }
    }
    return null;
  }

  public ArrayList<Photo> getPhotoFeed() {
    ArrayList<Photo> photos = new ArrayList<>();
    User currentUser = getCurrentUser();
    for (Photo photo : mPhotos) {

      // Add current user photos
      if (photo.user.equals(mCurrentUserUid)) {
        photos.add(photo);
      }

      // Add friend photos
      for (String friendUid : currentUser.following.keySet()) {
        if (photo.user.equals(friendUid)) {
          photos.add(photo);
        }
      }

    }

    Collections.sort(photos, new PhotoDateComparator());

    return photos;
  }

  public Photo getPhoto(String uid) {
    for (Photo photo : mPhotos) {
      if (photo.uid.equals(uid)) {
        return photo;
      }
    }
    return null;
  }

  public void addOrUpdatePhoto(Photo photo) {
    Photo existingPhoto = getPhoto(photo.uid);
    if (existingPhoto == null) {
      mPhotos.add(photo);
      for (BittypicPhotoFeedListener listener : mPhotoFeedListeners) {
        listener.onPhotoAdded(photo);
      }
    } else {
      existingPhoto.likes = photo.likes;
      for (BittypicPhotoFeedListener listener : mPhotoFeedListeners) {
        listener.onPhotoUpdated(photo);
      }
    }
  }

  public ArrayList<User> getFriends() {
    ArrayList<User> users = new ArrayList<>();
    User currentUser = getCurrentUser();
    for (String key : currentUser.facebookFriends.keySet()) {
      User user = getFacebookUser(key);
      if (user != null) {
        users.add(user);
      }
    }
    return users;
  }

  public interface BittypicAuthStateListener {
    void onUserLoggedIn(User user);
  }

  public interface BittypicPhotoFeedListener {
    void onPhotoAdded(Photo photo);
    void onPhotoUpdated(Photo photo);
  }

  public interface BittypicUserListener {
    void onUserUpdated(User user);
  }

}
