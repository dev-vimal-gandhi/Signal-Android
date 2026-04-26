package com.servalabs.chat.migrations;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.StickerTable;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.JobManager;
import com.servalabs.chat.jobs.MultiDeviceStickerPackOperationJob;
import com.servalabs.chat.jobs.StickerPackDownloadJob;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.stickers.BlessedPacks;
import com.servalabs.chat.util.TextSecurePreferences;

public class StickerLaunchMigrationJob extends MigrationJob {

  public static final String KEY = "StickerLaunchMigrationJob";

  StickerLaunchMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private StickerLaunchMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void performMigration() {
    installPack(context, BlessedPacks.ZOZO);
    installPack(context, BlessedPacks.BANDIT);
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  private static void installPack(@NonNull Context context, @NonNull BlessedPacks.Pack pack) {
    JobManager   jobManager      = AppDependencies.getJobManager();
    StickerTable stickerDatabase = SignalDatabase.stickers();

    if (stickerDatabase.isPackAvailableAsReference(pack.getPackId())) {
      stickerDatabase.markPackAsInstalled(pack.getPackId(), false);
    }

    jobManager.add(StickerPackDownloadJob.forInstall(pack.getPackId(), pack.getPackKey(), false));

    if (SignalStore.account().isMultiDevice()) {
      jobManager.add(new MultiDeviceStickerPackOperationJob(pack.getPackId(), pack.getPackKey(), MultiDeviceStickerPackOperationJob.Type.INSTALL));
    }
  }

  public static class Factory implements Job.Factory<StickerLaunchMigrationJob> {
    @Override
    public @NonNull
    StickerLaunchMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new StickerLaunchMigrationJob(parameters);
    }
  }
}
