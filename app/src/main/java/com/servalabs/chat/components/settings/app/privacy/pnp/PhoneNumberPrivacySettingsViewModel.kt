package com.servalabs.chat.components.settings.app.privacy.pnp

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobs.ProfileUploadJob
import com.servalabs.chat.jobs.RefreshAttributesJob
import com.servalabs.chat.jobs.RefreshOwnProfileJob
import com.servalabs.chat.keyvalue.PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode
import com.servalabs.chat.keyvalue.PhoneNumberPrivacyValues.PhoneNumberSharingMode
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.storage.StorageSyncHelper
import kotlin.time.Duration.Companion.seconds

class PhoneNumberPrivacySettingsViewModel : ViewModel() {

  private val _state = mutableStateOf(
    PhoneNumberPrivacySettingsState(
      phoneNumberSharing = SignalStore.phoneNumberPrivacy.isPhoneNumberSharingEnabled,
      discoverableByPhoneNumber = SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode != PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    )
  )

  val state: State<PhoneNumberPrivacySettingsState> = _state

  init {
    viewModelScope.launch(Dispatchers.IO) {
      while (isActive) {
        refresh()
        delay(5.seconds)
      }
    }
  }

  fun setNobodyCanSeeMyNumber() {
    setPhoneNumberSharingEnabled(false)
  }

  fun setEveryoneCanSeeMyNumber() {
    setPhoneNumberSharingEnabled(true)
    setDiscoverableByPhoneNumber(true)
  }

  fun setNobodyCanFindMeByMyNumber() {
    setDiscoverableByPhoneNumber(false)
  }

  fun setEveryoneCanFindMeByMyNumber() {
    setDiscoverableByPhoneNumber(true)
  }

  private fun setPhoneNumberSharingEnabled(phoneNumberSharingEnabled: Boolean) {
    SignalStore.phoneNumberPrivacy.phoneNumberSharingMode = if (phoneNumberSharingEnabled) PhoneNumberSharingMode.EVERYBODY else PhoneNumberSharingMode.NOBODY
    SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
    StorageSyncHelper.scheduleSyncForDataChange()
    AppDependencies.jobManager.add(ProfileUploadJob())
    refresh()
  }

  private fun setDiscoverableByPhoneNumber(discoverable: Boolean) {
    SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode = if (discoverable) PhoneNumberDiscoverabilityMode.DISCOVERABLE else PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
    StorageSyncHelper.scheduleSyncForDataChange()
    AppDependencies.jobManager.startChain(RefreshAttributesJob()).then(RefreshOwnProfileJob()).enqueue()
    refresh()
  }

  fun refresh() {
    _state.value = PhoneNumberPrivacySettingsState(
      phoneNumberSharing = SignalStore.phoneNumberPrivacy.isPhoneNumberSharingEnabled,
      discoverableByPhoneNumber = SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode != PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    )
  }
}
