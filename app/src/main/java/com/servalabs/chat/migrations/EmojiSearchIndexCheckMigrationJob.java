package com.servalabs.chat.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.SqlUtil;
import com.servalabs.chat.database.EmojiSearchTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob;
import com.servalabs.chat.keyvalue.SignalStore;

/**
 * Schedules job to get the latest emoji search index if it's empty.
 */
public final class EmojiSearchIndexCheckMigrationJob extends MigrationJob {

  public static final String KEY = "EmojiSearchIndexCheckMigrationJob";

  EmojiSearchIndexCheckMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private EmojiSearchIndexCheckMigrationJob(@NonNull Parameters parameters) {
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
    if (SqlUtil.isEmpty(SignalDatabase.getRawDatabase(), EmojiSearchTable.TABLE_NAME)) {
      SignalStore.emoji().clearSearchIndexMetadata();
      EmojiSearchIndexDownloadJob.scheduleImmediately();
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<EmojiSearchIndexCheckMigrationJob> {
    @Override
    public @NonNull EmojiSearchIndexCheckMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new EmojiSearchIndexCheckMigrationJob(parameters);
    }
  }
}
