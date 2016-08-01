package com.sixoverground.bittypic.services;

import android.net.Uri;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.Photo;
import com.sixoverground.bittypic.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class BittypicService {

  private static final String TAG = "BittypicService";

  private static BittypicService sInstance;

  private DatabaseReference mDatabase;
  private HashMap<String, DatabaseReference> mUserRefs;
  private HashMap<String, ChildEventListener> mUserPhotoListeners;
  private HashMap<String, DatabaseReference> mPhotoRefs;
  private HashMap<String, ChildEventListener> mUserFriendListeners;

  private BittypicService() {
    mDatabase = FirebaseDatabase.getInstance().getReference();
    mUserRefs = new HashMap<>();
    mUserPhotoListeners = new HashMap<>();
    mPhotoRefs = new HashMap<>();
    mUserFriendListeners = new HashMap<>();
  }

  public static synchronized BittypicService getInstance() {
    if (sInstance == null) sInstance = new BittypicService();
    return sInstance;
  }

  public void getOrCreateCurrentUser(final FirebaseUser firebaseUser) {
    String key = firebaseUser.getUid();
    if (mUserRefs.get(key) == null) {
      DatabaseReference ref = mDatabase.child("users").child(key);
      mUserRefs.put(key, ref);
      ref.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.exists()) {
            User user = dataSnapshot.getValue(User.class);
            login(user);
          } else {
            createCurrentUser(firebaseUser);
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
      });
    } else {
      User user = BittypicModel.getInstance().getUser(key);
      if (user != null) {
        login(user);
      }

    }
  }

  public void createPhoto(Photo photo, final BitlyServiceListener listener) {
    String key = mDatabase.child("photos").push().getKey();
    photo.uid = key;
    photo.user = BittypicModel.getInstance().getCurrentUser().uid;
    Map<String, Object> photoValues = photo.toMap();

    Map<String, Object> childUpdates = new HashMap<>();
    childUpdates.put("/photos/" + key, photoValues);
    childUpdates.put("/users/" + photo.user + "/photos/" + key, true);

    mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError != null) {
          listener.onBitlyServiceFailure();
        } else {
          listener.onBitlyServiceSuccess();
        }
      }
    });
  }

  public void likePhoto(final Photo photo) {
    final User user = BittypicModel.getInstance().getCurrentUser();

    DatabaseReference photoRef = mDatabase.child("photos").child(photo.uid);
    photoRef.runTransaction(new Transaction.Handler() {
      @Override
      public Transaction.Result doTransaction(MutableData mutableData) {
        Photo p = mutableData.getValue(Photo.class);
        if (p == null) {
          return Transaction.success(mutableData);
        }

        if (p.likes.containsKey(user.uid)) {
          p.likes.remove(user.uid);
        } else {
          p.likes.put(user.uid, true);

          User photoUser = BittypicModel.getInstance().getUser(p.user);
          if (photoUser != null) {
            String title = photoUser.displayName + " liked your photo";
            String message = "Touch to view your photo";
            sendNotification(photoUser, title, message, null);
          }
        }

        mutableData.setValue(p);
        return Transaction.success(mutableData);
      }

      @Override
      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        if (databaseError != null) {
        } else {
        }
      }
    });

    DatabaseReference userRef = mDatabase.child("users").child(user.uid);
    userRef.runTransaction(new Transaction.Handler() {
      @Override
      public Transaction.Result doTransaction(MutableData mutableData) {
        User u = mutableData.getValue(User.class);
        if (u == null) {
          return Transaction.success(mutableData);
        }

        if (u.likes.containsKey(photo.uid)) {
          u.likes.remove(photo.uid);
        } else {
          u.likes.put(photo.uid, true);
        }

        mutableData.setValue(u);
        return Transaction.success(mutableData);
      }

      @Override
      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        if (databaseError != null) {
        } else {
        }
      }
    });
  }

  public void followUser(final User friend) {
    final User currentUser = BittypicModel.getInstance().getCurrentUser();

    DatabaseReference currentUserRef = mDatabase.child("users").child(currentUser.uid);
    currentUserRef.runTransaction(new Transaction.Handler() {
      @Override
      public Transaction.Result doTransaction(MutableData mutableData) {
        User u = mutableData.getValue(User.class);
        if (u == null) {
          return Transaction.success(mutableData);
        }

        if (u.following.containsKey(friend.uid)) {
          u.following.remove(friend.uid);
        } else {
          u.following.put(friend.uid, true);
        }

        mutableData.setValue(u);
        return Transaction.success(mutableData);
      }

      @Override
      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        if (databaseError != null) {
        } else {
        }
      }
    });

    DatabaseReference friendRef = mDatabase.child("users").child(friend.uid);
    friendRef.runTransaction(new Transaction.Handler() {
      @Override
      public Transaction.Result doTransaction(MutableData mutableData) {
        User u = mutableData.getValue(User.class);
        if (u == null) {
          return Transaction.success(mutableData);
        }

        if (u.followers.containsKey(currentUser.uid)) {
          u.followers.remove(currentUser.uid);
        } else {
          u.followers.put(currentUser.uid, true);
        }

        mutableData.setValue(u);
        return Transaction.success(mutableData);
      }

      @Override
      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        if (databaseError != null) {
        } else {
        }
      }
    });
  }

  public void registerDeviceToken() {
    User currentUser = BittypicModel.getInstance().getCurrentUser();
    String token = BittypicModel.getInstance().getDeviceToken();
    if (currentUser != null && token != null) {
      mDatabase.child("users").child(currentUser.uid).child("deviceToken").setValue(token, new DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
          if (databaseError != null) {
          } else {
          }
        }
      });
    }
  }

  private void createCurrentUser(FirebaseUser firebaseUser) {
    String uid = firebaseUser.getUid();
    String displayName = firebaseUser.getDisplayName();
    String email = firebaseUser.getEmail();
    Uri photoUrl = firebaseUser.getPhotoUrl();
    String photoUrlString = null;
    if (photoUrl != null) photoUrlString = photoUrl.toString();
    String providerId = null;
    String providerUid = null;
    for (UserInfo profile : firebaseUser.getProviderData()) {
      providerId = profile.getProviderId();
      providerUid = profile.getUid();
    }

    final User user = new User(uid, displayName, email, photoUrlString, providerId, providerUid);

    Map<String, Object> userValues = user.toMap();
    Map<String, Object> childUpdates = new HashMap<>();
    childUpdates.put("/users/" + user.uid, userValues);
    mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError != null) {
        } else {
          login(user);
        }
      }
    });
  }

  private void login(User user) {
    BittypicModel.getInstance().login(user);
    registerDeviceToken();
    observeUserPhotos(user.uid);
    observeUserFriends(user.uid);
    getFacebookFriends();
  }

  private void observeUserPhotos(String uid) {
    if (mUserPhotoListeners.get(uid) == null) {
      ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
          String key = dataSnapshot.getKey();
          observePhoto(key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      };
      mUserPhotoListeners.put(uid, childEventListener);
      mDatabase.child("users").child(uid).child("photos").addChildEventListener(childEventListener);
    }
  }

  private void observeUserFriends(String uid) {
    if (mUserFriendListeners.get(uid) == null) {
      ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
          String key = dataSnapshot.getKey();
          observeUser(key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      };
      mUserFriendListeners.put(uid, childEventListener);
      mDatabase.child("users").child(uid).child("following").addChildEventListener(childEventListener);
    }
  }

  private void observePhoto(String uid) {
    if (mPhotoRefs.get(uid) == null) {
      DatabaseReference databaseReference = mDatabase.child("photos").child(uid);
      mPhotoRefs.put(uid, databaseReference);
      databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          Photo photo = dataSnapshot.getValue(Photo.class);
          BittypicModel.getInstance().addOrUpdatePhoto(photo);
          observeUser(photo.user);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }
  }

  private void observeUser(final String uid) {
    if (uid != null && mUserRefs.get(uid) == null) {
      DatabaseReference databaseReference = mDatabase.child("users").child(uid);
      mUserRefs.put(uid, databaseReference);
      databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          User user = dataSnapshot.getValue(User.class);
          BittypicModel.getInstance().addOrUpdateUser(user);
          observeUserPhotos(uid);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }
  }

  private void getFacebookFriends() {
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    if (accessToken.getPermissions().contains("user_friends")) {
      GraphRequest request = GraphRequest.newMyFriendsRequest(accessToken, new GraphRequest.GraphJSONArrayCallback() {
        @Override
        public void onCompleted(JSONArray objects, GraphResponse response) {
          if (response.getError() != null) {
            // TODO: handle error
          } else {
            HashMap<String, Boolean> friends = new HashMap<>();
            for (int i = 0; i < objects.length(); i++) {
              try {
                JSONObject object = objects.getJSONObject(i);
                String id = object.getString("id");
                String name = object.getString("name");
                friends.put(id, true);
              } catch (JSONException e) {
              }
            }
            saveFacebookFriends(friends);
          }
        }
      });
      request.executeAsync();
    }
  }

  private void saveFacebookFriends(Map<String, Boolean> friends) {
    User user = BittypicModel.getInstance().getCurrentUser();
    mDatabase.child("users").child(user.uid).child("facebookFriends").setValue(friends, new DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError != null) {
        } else {
          observeFacebookFriends();
        }
      }
    });
  }

  private void observeFacebookFriends() {
    Map<String, Boolean> facebookFriends = BittypicModel.getInstance().getCurrentUser().facebookFriends;
    for (String providerUid : facebookFriends.keySet()) {
      mDatabase.child("users").orderByChild("providerUid").equalTo(providerUid).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.hasChildren()) {
            DataSnapshot userRef = dataSnapshot.getChildren().iterator().next();
            User user = userRef.getValue(User.class);
            BittypicModel.getInstance().addOrUpdateUser(user);
            observeUser(user.uid);
          } else {
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
      });
    }
  }

  private void sendNotification(User toUser, String title, String message, JSONObject data) {

    // TODO: make sure we're not sending to ourselves

    if (toUser.deviceToken != null) {
      try {
        JSONObject json = new JSONObject("{'contents': {'en':'" + message + "'}, 'include_player_ids': ['" + toUser.deviceToken + "'], 'headings': {'en': '" + title + "'}}");
        if (data != null) json.put("data", data);
        OneSignal.postNotification(json,
            new OneSignal.PostNotificationResponseHandler() {
              @Override
              public void onSuccess(JSONObject response) {
              }

              @Override
              public void onFailure(JSONObject response) {
              }
            });
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

  }

  public interface BitlyServiceListener {
    void onBitlyServiceSuccess();
    void onBitlyServiceFailure();
  }

}
