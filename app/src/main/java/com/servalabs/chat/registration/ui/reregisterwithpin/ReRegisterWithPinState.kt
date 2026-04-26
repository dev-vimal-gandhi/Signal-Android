/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.ui.reregisterwithpin

import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.lock.v2.PinKeyboardType

data class ReRegisterWithPinState(
  val isLocalVerification: Boolean = false,
  val hasIncorrectGuess: Boolean = false,
  val localPinMatches: Boolean = false,
  val pinKeyboardType: PinKeyboardType = SignalStore.pin.keyboardType
)
