package com.servalabs.chat.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import com.servalabs.chat.crypto.PreKeyUtil;
import com.servalabs.chat.crypto.storage.PreKeyMetadataStore;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.keyvalue.SignalStore;

/**
 * Deprecated. Only exists for previously-enqueued jobs. 
 * Use {@link PreKeyUtil#cleanSignedPreKeys(SignalProtocolStore, PreKeyMetadataStore)} instead.
 */
@Deprecated
public class CleanPreKeysJob extends BaseJob {

  public static final String KEY = "CleanPreKeysJob";

  private static final String TAG = Log.tag(CleanPreKeysJob.class);

  private CleanPreKeysJob(@NonNull Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public @Nullable byte[] serialize() {
    return null;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() {
    PreKeyUtil.cleanSignedPreKeys(AppDependencies.getProtocolStore().aci(), SignalStore.account().aciPreKeys());
    PreKeyUtil.cleanSignedPreKeys(AppDependencies.getProtocolStore().pni(), SignalStore.account().pniPreKeys());
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception throwable) {
    return false;
  }

  @Override
  public void onFailure() {
    Log.w(TAG, "Failed to execute clean signed prekeys task.");
  }

  public static final class Factory implements Job.Factory<CleanPreKeysJob> {
    @Override
    public @NonNull CleanPreKeysJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new CleanPreKeysJob(parameters);
    }
  }
}
