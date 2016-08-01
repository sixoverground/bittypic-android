package com.sixoverground.bittypic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class SquareImageView extends ImageView {

  public SquareImageView(Context context) {
    super(context);
  }

  public SquareImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredWidth();
    setMeasuredDimension(width, height);
  }

}
