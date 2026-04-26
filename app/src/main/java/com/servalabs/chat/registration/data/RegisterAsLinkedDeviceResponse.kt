/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.data

data class RegisterAsLinkedDeviceResponse(
  val deviceId: Int,
  val accountRegistrationResult: AccountRegistrationResult
)
