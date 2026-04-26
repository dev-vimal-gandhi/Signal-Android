/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.devicetransfer.newdevice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.servalabs.chat.database.model.databaseprotos.RestoreDecisionState
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobs.ReclaimUsernameAndLinkJob
import com.servalabs.chat.keyvalue.Completed
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.registration.data.RegistrationRepository
import com.servalabs.chat.registration.util.RegistrationUtil

class NewDeviceTransferViewModel : ViewModel() {
  fun onRestoreComplete(context: Context, onComplete: () -> Unit) {
    viewModelScope.launch {
      SignalStore.registration.localRegistrationMetadata?.let { metadata ->
        RegistrationRepository.registerAccountLocally(context, metadata)
        SignalStore.registration.localRegistrationMetadata = null
        RegistrationUtil.maybeMarkRegistrationComplete()

        SignalStore.misc.needsUsernameRestore = true
        AppDependencies.jobManager.add(ReclaimUsernameAndLinkJob())
      }

      SignalStore.registration.restoreDecisionState = RestoreDecisionState.Completed

      withContext(Dispatchers.Main) {
        onComplete()
      }
    }
  }
}
