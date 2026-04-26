/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.util;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.backup.v2.BackupRepository;
import com.servalabs.chat.backup.v2.MessageBackupTier;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.ArchiveBackupIdReservationJob;
import com.servalabs.chat.jobs.DirectoryRefreshJob;
import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob;
import com.servalabs.chat.jobs.PostRegistrationBackupRedemptionJob;
import com.servalabs.chat.jobs.RefreshAttributesJob;
import com.servalabs.chat.jobs.StorageSyncJob;
import com.servalabs.chat.keyvalue.PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode;
import com.servalabs.chat.keyvalue.RestoreDecisionStateUtil;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.util.RemoteConfig;

public final class RegistrationUtil {

  private static final String TAG = Log.tag(RegistrationUtil.class);

  private RegistrationUtil() {}

  /**
   * There's several events where a registration may or may not be considered complete based on what
   * path a user has taken. This will only truly mark registration as complete if all of the
   * requirements are met.
   */
  public static void maybeMarkRegistrationComplete() {
    if (!SignalStore.registration().isRegistrationComplete() &&
        SignalStore.account().isRegistered() &&
        !Recipient.self().getProfileName().isEmpty() &&
        (SignalStore.svr().hasPin() || SignalStore.svr().hasOptedOut() || SignalStore.account().isLinkedDevice()) &&
        RestoreDecisionStateUtil.isTerminal(SignalStore.registration().getRestoreDecisionState()))
    {
      Log.i(TAG, "Marking registration completed.", new Throwable());
      SignalStore.registration().markRegistrationComplete();
      SignalStore.registration().setLocalRegistrationMetadata(null);
      SignalStore.registration().setRestoreMethodToken(null);

      if (SignalStore.phoneNumberPrivacy().getPhoneNumberDiscoverabilityMode() == PhoneNumberDiscoverabilityMode.UNDECIDED) {
        Log.w(TAG, "Phone number discoverability mode is still UNDECIDED. Setting to DISCOVERABLE.");
        SignalStore.phoneNumberPrivacy().setPhoneNumberDiscoverabilityMode(PhoneNumberDiscoverabilityMode.DISCOVERABLE);
      }

      AppDependencies.getJobManager().startChain(new RefreshAttributesJob())
                     .then(StorageSyncJob.forRemoteChange())
                     .then(new DirectoryRefreshJob(false))
                     .enqueue();

      SignalStore.emoji().clearSearchIndexMetadata();
      EmojiSearchIndexDownloadJob.scheduleImmediately();


      BackupRepository.INSTANCE.resetInitializedStateAndAuthCredentials();
      AppDependencies.getJobManager().add(new ArchiveBackupIdReservationJob());
      AppDependencies.getJobManager().add(new PostRegistrationBackupRedemptionJob());

    } else if (!SignalStore.registration().isRegistrationComplete()) {
      Log.i(TAG, "Registration is not yet complete.", new Throwable());
    }
  }
}
