package com.servalabs.chat.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob;

public class LocaleChangedReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    NotificationChannels.getInstance().onLocaleChanged();
    EmojiSearchIndexDownloadJob.scheduleImmediately();
  }
}
