package com.sixoverground.bittypic.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sixoverground.bittypic.R;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.models.User;

import java.util.ArrayList;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class FriendRecyclerViewAdapter extends RecyclerView.Adapter<FriendRecyclerViewAdapter.ViewHolder> {

  private static final String TAG = "FriendRecyclerViewAdapt";

  private final OnFriendClickListener mListener;

  private ArrayList<User> mValues = new ArrayList<>();

  public FriendRecyclerViewAdapter(OnFriendClickListener listener) {
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_friend_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    User user = mValues.get(position);
    holder.mItem = user;

    Context profileContext = holder.mProfileImage.getContext();
    Glide.with(profileContext).clear(holder.mProfileImage);
    RequestOptions options = new RequestOptions()
        .centerCrop();
    Glide
        .with(profileContext)
        .load(user.photoUrl)
        .apply(options)
        .into(holder.mProfileImage);

    holder.mDisplayNameText.setText(user.displayName);

    User currentUser = BittypicModel.getInstance().getCurrentUser();
    if (currentUser.following.containsKey(user.uid)) {
      String followingString = holder.mFollowButton.getContext().getResources().getString(R.string.action_following);
      holder.mFollowButton.setText(followingString);
    } else {
      String followString = holder.mFollowButton.getContext().getResources().getString(R.string.action_follow);
      holder.mFollowButton.setText(followString);
    }

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mListener != null) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onFriendClick(holder.mItem);
        }
      }
    });

    holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mListener != null) {
          mListener.onFriendFollowClick(holder.mItem);
        }
      }
    });


  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public void setItems(ArrayList<User> friends) {
    mValues = friends;
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final ImageView mProfileImage;
    public final TextView mDisplayNameText;
    public final Button mFollowButton;
    public User mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mProfileImage = (ImageView) view.findViewById(R.id.image_profile);
      mDisplayNameText = (TextView) view.findViewById(R.id.text_display_name);
      mFollowButton = (Button) view.findViewById(R.id.button_follow);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mDisplayNameText.getText() + "'";
    }
  }

  public interface OnFriendClickListener {
    void onFriendClick(User item);
    void onFriendFollowClick(User item);
  }
}
