package com.servalabs.chat.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobs.RefreshAttributesJob;
import com.servalabs.chat.jobs.RefreshOwnProfileJob;
import com.servalabs.chat.jobs.StorageForcePushJob;
import com.servalabs.chat.keyvalue.SignalStore;

/**
 * We changed some details of what it means to opt-out of a PIN. This ensures that users who went
 * through the previous opt-out flow are now in the same state as users who went through the new
 * opt-out flow.
 */
public final class PinOptOutMigration extends MigrationJob {

  private static final String TAG = Log.tag(PinOptOutMigration.class);

  public static final String KEY = "PinOptOutMigration";

  PinOptOutMigration() {
    this(new Parameters.Builder().build());
  }

  private PinOptOutMigration(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  boolean isUiBlocking() {
    return false;
  }

  @Override
  void performMigration() {
    if (SignalStore.svr().hasOptedOut() && SignalStore.svr().hasPin()) {
      Log.w(TAG, "Discovered a legacy opt-out user! Resetting the state.");

      SignalStore.svr().optOut();
      AppDependencies.getJobManager().startChain(new RefreshAttributesJob())
                     .then(new RefreshOwnProfileJob())
                     .then(new StorageForcePushJob())
                     .enqueue();
    } else if (SignalStore.svr().hasOptedOut()) {
      Log.i(TAG, "Discovered an opt-out user, but they're already in a good state. No action required.");
    } else {
      Log.i(TAG, "Discovered a normal PIN user. No action required.");
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  public static class Factory implements Job.Factory<PinOptOutMigration> {
    @Override
    public @NonNull PinOptOutMigration create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new PinOptOutMigration(parameters);
    }
  }
}
