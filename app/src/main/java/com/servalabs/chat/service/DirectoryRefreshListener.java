package com.servalabs.chat.service;


import android.content.Context;

import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.DirectoryRefreshJob;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.util.RemoteConfig;
import com.servalabs.chat.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class DirectoryRefreshListener extends PersistentAlarmManagerListener {

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getDirectoryRefreshTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    if (scheduledTime != 0 && SignalStore.account().isRegistered()) {
      AppDependencies.getJobManager().add(new DirectoryRefreshJob(true));
    }

    long newTime;

    if (SignalStore.misc().isCdsBlocked()) {
      newTime = Math.min(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(6),
                         SignalStore.misc().getCdsBlockedUtil());
    } else {
      newTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(RemoteConfig.cdsRefreshIntervalSeconds());
      TextSecurePreferences.setDirectoryRefreshTime(context, newTime);
    }

    TextSecurePreferences.setDirectoryRefreshTime(context, newTime);

    return newTime;
  }

  public static void schedule(Context context) {
    new DirectoryRefreshListener().onReceive(context, getScheduleIntent());
  }
}
