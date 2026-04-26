package com.servalabs.chat.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobs.DownloadLatestEmojiDataJob;
import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob;

/**
 * Schedules jobs to get the latest emoji and search index.
 */
public final class EmojiDownloadMigrationJob extends MigrationJob {

  public static final String KEY = "EmojiDownloadMigrationJob";

  EmojiDownloadMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private EmojiDownloadMigrationJob(@NonNull Parameters parameters) {
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
    AppDependencies.getJobManager().add(new DownloadLatestEmojiDataJob(false));
    EmojiSearchIndexDownloadJob.scheduleImmediately();
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<EmojiDownloadMigrationJob> {
    @Override
    public @NonNull EmojiDownloadMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new EmojiDownloadMigrationJob(parameters);
    }
  }
}
