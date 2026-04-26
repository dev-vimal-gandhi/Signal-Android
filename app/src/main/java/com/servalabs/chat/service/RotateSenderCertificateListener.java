package com.servalabs.chat.service;


import android.content.Context;

import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.RotateCertificateJob;
import com.servalabs.chat.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class RotateSenderCertificateListener extends PersistentAlarmManagerListener {

  private static final long INTERVAL = TimeUnit.DAYS.toMillis(1);

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getUnidentifiedAccessCertificateRotationTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    AppDependencies.getJobManager().add(new RotateCertificateJob());

    long nextTime = System.currentTimeMillis() + INTERVAL;
    TextSecurePreferences.setUnidentifiedAccessCertificateRotationTime(context, nextTime);

    return nextTime;
  }

  public static void schedule(Context context) {
    new RotateSenderCertificateListener().onReceive(context, getScheduleIntent());
  }

}
