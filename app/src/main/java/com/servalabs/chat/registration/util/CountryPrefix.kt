/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.util

data class CountryPrefix(val digits: Int, val regionCode: String) {
  override fun toString(): String {
    return "+$digits"
  }
}
