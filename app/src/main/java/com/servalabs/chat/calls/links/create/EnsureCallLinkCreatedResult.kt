/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.calls.links.create

import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.service.webrtc.links.CreateCallLinkResult

sealed interface EnsureCallLinkCreatedResult {
  data class Success(val recipient: Recipient) : EnsureCallLinkCreatedResult
  data class Failure(val failure: CreateCallLinkResult.Failure) : EnsureCallLinkCreatedResult
}
