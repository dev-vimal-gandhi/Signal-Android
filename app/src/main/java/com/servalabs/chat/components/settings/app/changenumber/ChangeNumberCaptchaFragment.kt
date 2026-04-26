/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.changenumber

import androidx.fragment.app.activityViewModels
import com.servalabs.chat.registration.ui.captcha.CaptchaFragment

/**
 * Screen visible to the user when they are to solve a captcha. @see [CaptchaFragment]
 */
class ChangeNumberCaptchaFragment : CaptchaFragment() {
  private val viewModel by activityViewModels<ChangeNumberViewModel>()

  override fun handleCaptchaToken(token: String) {
    viewModel.setCaptchaResponse(token)
  }
}
