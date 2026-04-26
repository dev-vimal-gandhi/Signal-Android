/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.ui.restore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.signal.core.util.Stopwatch
import org.signal.core.util.logging.Log
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.enqueueBlocking
import com.servalabs.chat.jobmanager.runJobBlocking
import com.servalabs.chat.jobs.ProfileUploadJob
import com.servalabs.chat.jobs.ReclaimUsernameAndLinkJob
import com.servalabs.chat.jobs.StorageAccountRestoreJob
import com.servalabs.chat.jobs.StorageSyncJob
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.registration.data.RegistrationRepository
import com.servalabs.chat.registration.util.RegistrationUtil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object StorageServiceRestore {
  private val TAG = Log.tag(StorageServiceRestore::class)

  /**
   * Restore account data from Storage Service in a quasi-blocking manner. Uses existing jobs
   * to perform the restore but will not wait indefinitely for them to finish so may return prior
   * to completing the restore.
   */
  suspend fun restore() {
    withContext(Dispatchers.IO) {
      val stopwatch = Stopwatch("storage-service-restore")

      SignalStore.storageService.needsAccountRestore = false

      AppDependencies.jobManager.runJobBlocking(StorageAccountRestoreJob(), StorageAccountRestoreJob.LIFESPAN.milliseconds)
      stopwatch.split("account-restore")

      AppDependencies
        .jobManager
        .startChain(StorageSyncJob.forAccountRestore())
        .then(ReclaimUsernameAndLinkJob())
        .enqueueBlocking(10.seconds)
      stopwatch.split("storage-sync-restore")

      stopwatch.stop(TAG)

      val isMissingProfileData = RegistrationRepository.isMissingProfileData()

      RegistrationUtil.maybeMarkRegistrationComplete()
      if (!isMissingProfileData && SignalStore.account.isPrimaryDevice) {
        AppDependencies.jobManager.add(ProfileUploadJob())
      }
    }
  }
}
