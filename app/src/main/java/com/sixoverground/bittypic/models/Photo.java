package com.sixoverground.bittypic.models;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class Photo {
  public String uid;
  public String url;
  public String caption;
  public String user;

  public Map<String, Boolean> likes = new HashMap<>();

  public Photo() {}

  public Map<String, Object> toMap() {
    HashMap<String, Object> result = new HashMap<>();
    result.put("uid", uid);
    result.put("url", url);
    result.put("caption", caption);
    result.put("user", user);
    return result;
  }
}
