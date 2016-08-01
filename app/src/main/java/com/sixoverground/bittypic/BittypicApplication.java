package com.sixoverground.bittypic;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.onesignal.OneSignal;
import com.sixoverground.bittypic.models.BittypicModel;
import com.sixoverground.bittypic.services.BittypicService;

import org.json.JSONObject;

/**
 * Created by Craig Phares on 8/1/16.
 * Copyright © 2016 Six Overground. All rights reserved.
 */
public class BittypicApplication extends Application {

  private static final String TAG = "BittypicApplication";

  private static BittypicApplication sInstance;

  private Context mContext;

  public static synchronized BittypicApplication getInstance() {
    return sInstance;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    sInstance = this;
    mContext = getApplicationContext();

    FacebookSdk.sdkInitialize(getApplicationContext());
    AppEventsLogger.activateApp(this);

    OneSignal.startInit(this)
        .setNotificationOpenedHandler(new BittypicNotificationOpenedHandler())
        .init();

    OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
      @Override
      public void idsAvailable(String userId, String registrationId) {
        BittypicModel.getInstance().setDeviceToken(userId);
        BittypicService.getInstance().registerDeviceToken();
      }
    });
  }

  public Context getContext() {
    return mContext;
  }

  private class BittypicNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    @Override
    public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
      try {
        if (additionalData != null) {
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }

      // The following can be used to open an Activity of your choice.
      /*
      Intent intent = new Intent(getApplication(), YourActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
      */
      // Follow the instructions in the link below to prevent the launcher Activity from starting.
      // https://documentation.onesignal.com/docs/android-notification-customizations#changing-the-open-action-of-a-notification

    }
  }

}
