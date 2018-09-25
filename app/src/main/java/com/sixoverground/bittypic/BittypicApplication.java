package com.sixoverground.bittypic;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
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
    public void notificationOpened(OSNotificationOpenResult result) {
      OSNotificationAction.ActionType actionType = result.action.type;
//      JSONObject data = result.notification.payload.additionalData;
//      if (data != null) {
//
//      }
      if (actionType == OSNotificationAction.ActionType.ActionTaken)
        Log.i(TAG, "Button pressed with id: " + result.action.actionID);
    }
  }

}
