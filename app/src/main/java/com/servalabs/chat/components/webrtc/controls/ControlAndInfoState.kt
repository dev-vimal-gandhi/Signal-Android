/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.webrtc.controls

import androidx.compose.runtime.Immutable
import com.servalabs.chat.database.CallLinkTable

@Immutable
data class ControlAndInfoState(
  val callLink: CallLinkTable.CallLink? = null,
  val isGroupAdmin: Boolean = false,
  val resetScrollState: Long = 0
) {
  fun isSelfAdmin(): Boolean {
    return callLink?.credentials?.adminPassBytes != null || isGroupAdmin
  }
}
