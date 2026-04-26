/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.dependencies

import org.signal.core.ui.CoreUiDependencies
import com.servalabs.chat.BuildConfig
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.util.TextSecurePreferences

object CoreUiDependenciesProvider : CoreUiDependencies.Provider {
  override fun providePackageId(): String {
    return BuildConfig.APPLICATION_ID
  }

  override fun provideIsIncognitoKeyboardEnabled(): Boolean {
    return TextSecurePreferences.isIncognitoKeyboardEnabled(AppDependencies.application)
  }

  override fun provideIsScreenSecurityEnabled(): Boolean {
    return TextSecurePreferences.isScreenSecurityEnabled(AppDependencies.application)
  }

  override fun provideForceSplitPane(): Boolean {
    return SignalStore.internal.forceSplitPane
  }
}
