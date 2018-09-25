package com.sixoverground.bittypic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sixoverground.bittypic.R;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.Photo;
import com.sixoverground.bittypic.models.User;

import java.util.ArrayList;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder> {

  private static final String TAG = "PhotoRecyclerViewAdapt";

  private final OnPhotoClickListener mListener;

  private ArrayList<Photo> mValues = new ArrayList<>();

  public PhotoRecyclerViewAdapter(OnPhotoClickListener listener) {
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_photo_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    Photo photo = mValues.get(position);
    holder.mItem = photo;

    Context photoContext = holder.mPhotoImage.getContext();
    Glide.with(photoContext).clear(holder.mPhotoImage);
    RequestOptions options = new RequestOptions()
        .centerCrop();
    Glide
        .with(photoContext)
        .load(photo.url)
        .apply(options)
        .into(holder.mPhotoImage);

    CharSequence caption = "";

    if (photo.likes.size() > 0) {
      Spannable likesSpannable = new SpannableString(photo.likes.size() + " likes");
      ClickableSpan likesSpan = new ClickableSpan() {
        @Override
        public void onClick(View view) {
          // TODO: show likes
        }

        @Override
        public void updateDrawState(TextPaint ds) {
          super.updateDrawState(ds);
          ds.setUnderlineText(false);
          ds.setTypeface(Typeface.DEFAULT_BOLD);
          ds.setColor(ContextCompat.getColor(holder.mCaptionText.getContext(), android.R.color.black));
        }
      };
      likesSpannable.setSpan(likesSpan, 0, likesSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      caption = TextUtils.concat(caption, likesSpannable, "\n\n");
    }

    User user = BittypicModel.getInstance().getUser(photo.user);
    if (user != null) {
      Context profileContext = holder.mProfileImage.getContext();
      Glide.with(profileContext).clear(holder.mProfileImage);
      Glide
          .with(profileContext)
          .load(user.photoUrl)
          .apply(options)
          .into(holder.mProfileImage);

      holder.mDisplayNameText.setText(user.displayName);

      Spannable userSpannable = new SpannableString(user.displayName + " " + photo.caption);
      ClickableSpan userSpan = new ClickableSpan() {
        @Override
        public void onClick(View view) {
          // TODO: show user profile
        }

        @Override
        public void updateDrawState(TextPaint ds) {
          super.updateDrawState(ds);
          ds.setUnderlineText(false);
          ds.setTypeface(Typeface.DEFAULT_BOLD);
          ds.setColor(ContextCompat.getColor(holder.mCaptionText.getContext(), android.R.color.black));
        }
      };
      userSpannable.setSpan(userSpan, 0, user.displayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//      holder.mCaptionText.setText(userSpannable);
//      spannable = TextUtils.concat(spannable, userSpannable);
      caption = TextUtils.concat(caption, userSpannable, "\n\n");
    }

    holder.mCaptionText.setMovementMethod(LinkMovementMethod.getInstance());
    holder.mCaptionText.setEnabled(true);
    holder.mCaptionText.setText(caption);

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mListener != null) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onPhotoClick(holder.mItem);
        }
      }
    });

    User currentUser = BittypicModel.getInstance().getCurrentUser();
    if (photo.likes.containsKey(currentUser.uid)) {
      holder.mLikeButton.setImageDrawable(ContextCompat.getDrawable(holder.mLikeButton.getContext(), R.drawable.ic_favorite_black_24dp));
      holder.mLikeButton.setColorFilter(Color.argb(255, 204, 0, 0));
    } else {
      holder.mLikeButton.setImageDrawable(ContextCompat.getDrawable(holder.mLikeButton.getContext(), R.drawable.ic_favorite_border_black_24dp));
      holder.mLikeButton.clearColorFilter();
    }

    holder.mLikeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mListener != null) {
          mListener.onPhotoLikeClick(holder.mItem);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public void setItems(ArrayList<Photo> photos) {
    mValues = photos;
    notifyDataSetChanged();
  }
//  public void addItem(Photo photo) {
//    if (!mValues.contains(photo)) {
//      mValues.add(photo);
//      notifyItemInserted(mValues.size() - 1);
//    }
//  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final ImageView mProfileImage;
    public final TextView mDisplayNameText;
    public final ImageView mPhotoImage;
    public final TextView mCaptionText;
    public final ImageButton mLikeButton;
    public Photo mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mProfileImage = (ImageView) view.findViewById(R.id.image_profile);
      mDisplayNameText = (TextView) view.findViewById(R.id.text_display_name);
      mPhotoImage = (ImageView) view.findViewById(R.id.image_photo);
      mCaptionText = (TextView) view.findViewById(R.id.text_caption);
      mLikeButton = (ImageButton) view.findViewById(R.id.button_like);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mCaptionText.getText() + "'";
    }
  }

  public interface OnPhotoClickListener {
    void onPhotoClick(Photo item);
    void onPhotoLikeClick(Photo item);
  }
}