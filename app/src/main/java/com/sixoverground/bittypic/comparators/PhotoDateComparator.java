package com.sixoverground.bittypic.comparators;

import com.sixoverground.bittypic.models.Photo;

import java.util.Comparator;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class PhotoDateComparator implements Comparator<Photo> {
  @Override
  public int compare(Photo o1, Photo o2) {
    return o2.uid.compareTo(o1.uid);
  }
}
