package com.servalabs.chat.components.settings.app.usernamelinks.main

import com.servalabs.chat.components.settings.app.usernamelinks.QrCodeState
import com.servalabs.chat.components.settings.app.usernamelinks.UsernameQrCodeColorScheme

/**
 * Represents the UI state of the [UsernameLinkSettingsFragment].
 */
data class UsernameLinkSettingsState(
  val activeTab: ActiveTab,
  val username: String,
  val usernameLinkState: UsernameLinkState,
  val qrCodeState: QrCodeState,
  val qrCodeColorScheme: UsernameQrCodeColorScheme,
  val qrScanResult: QrScanResult? = null,
  val usernameLinkResetResult: UsernameLinkResetResult? = null,
  val indeterminateProgress: Boolean = false
) {
  enum class ActiveTab {
    Code,
    Scan
  }
}
