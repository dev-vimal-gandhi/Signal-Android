/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.labs

import androidx.compose.runtime.Immutable

@Immutable
data class LabsSettingsState(
  val individualChatPlaintextExport: Boolean = false,
  val storyArchive: Boolean = false,
  val incognito: Boolean = false,
  val groupSuggestionsForMembers: Boolean = false,
  val betterSearch: Boolean = false,
  val autoLowerHand: Boolean = false,

  val starredMessages: Boolean = false
)
