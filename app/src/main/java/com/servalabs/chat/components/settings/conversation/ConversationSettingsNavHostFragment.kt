/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.conversation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.DSLSettingsActivity
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.recipients.RecipientId

class ConversationSettingsNavHostFragment : NavHostFragment() {

  companion object {
    suspend fun createArgs(recipientId: RecipientId): Bundle {
      val recipient = withContext(Dispatchers.IO) { Recipient.resolved(recipientId) }

      val args = if (recipient.isGroup) {
        ConversationSettingsFragmentArgs.Builder(null, recipient.requireGroupId(), null)
      } else {
        ConversationSettingsFragmentArgs.Builder(recipientId, null, null)
      }.build()

      return bundleOf(DSLSettingsActivity.ARG_START_BUNDLE to args.toBundle())
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    val args = requireArguments().getBundle(DSLSettingsActivity.ARG_START_BUNDLE)
    navController.setGraph(R.navigation.conversation_settings, args)
    super.onCreate(savedInstanceState)
  }
}
