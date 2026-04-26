package com.servalabs.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.MessageFetchJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    AppDependencies.getJobManager().add(new MessageFetchJob());
  }
}
