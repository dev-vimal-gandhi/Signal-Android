package com.servalabs.chat.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.JobManager;
import com.servalabs.chat.jobs.MultiDeviceKeysUpdateJob;
import com.servalabs.chat.jobs.MultiDeviceStorageSyncRequestJob;
import com.servalabs.chat.jobs.StorageSyncJob;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.util.TextSecurePreferences;

/**
 * Just runs a storage sync. Useful if you've started syncing a new field to storage service.
 */
public class StorageServiceMigrationJob extends MigrationJob {

  private static final String TAG = Log.tag(StorageServiceMigrationJob.class);

  public static final String KEY = "StorageServiceMigrationJob";

  StorageServiceMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private StorageServiceMigrationJob(@NonNull Parameters parameters) {
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
    if (SignalStore.account().getAci() == null) {
      Log.w(TAG, "Self not yet available.");
      return;
    }

    SignalDatabase.recipients().markNeedsSync(Recipient.self().getId());

    JobManager jobManager = AppDependencies.getJobManager();

    if (SignalStore.account().isMultiDevice()) {
      Log.i(TAG, "Multi-device.");
      jobManager.startChain(StorageSyncJob.forLocalChange())
                .then(new MultiDeviceStorageSyncRequestJob())
                .enqueue();
    } else {
      Log.i(TAG, "Single-device.");
      jobManager.add(StorageSyncJob.forLocalChange());
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<StorageServiceMigrationJob> {
    @Override
    public @NonNull StorageServiceMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new StorageServiceMigrationJob(parameters);
    }
  }
}
