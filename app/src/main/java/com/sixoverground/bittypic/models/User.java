package com.sixoverground.bittypic.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class User {
  public String uid;
  public String displayName;
  public String email;
  public String photoUrl;
  public String providerId;
  public String providerUid;
  public String deviceToken;

  public Map<String, Boolean> facebookFriends = new HashMap<>();
  public Map<String, Boolean> photos = new HashMap<>();
  public Map<String, Boolean> likes = new HashMap<>();
  public Map<String, Boolean> following = new HashMap<>();
  public Map<String, Boolean> followers = new HashMap<>();

  public User() {}

  public User(String uid, String displayName, String email, String photoUrl, String providerId, String providerUid) {
    this.uid = uid;
    this.displayName = displayName;
    this.email = email;
    this.photoUrl = photoUrl;
    this.providerId = providerId;
    this.providerUid = providerUid;
  }

  public Map<String, Object> toMap() {
    HashMap<String, Object> result = new HashMap<>();
    result.put("uid", uid);
    result.put("displayName", displayName);
    result.put("email", email);
    result.put("photoUrl", photoUrl);
    result.put("providerId", providerId);
    result.put("providerUid", providerUid);
    return result;
  }
}
