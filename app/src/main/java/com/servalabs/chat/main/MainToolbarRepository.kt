/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.main

import org.signal.core.util.concurrent.SignalExecutors
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.notifications.MarkReadReceiver

object MainToolbarRepository {
  /**
   * Mark all unread messages in the local database as read.
   */
  fun markAllMessagesRead() {
    SignalExecutors.BOUNDED.execute {
      val messageIds = SignalDatabase.threads.setAllThreadsRead()
      AppDependencies.messageNotifier.updateNotification(AppDependencies.application)
      MarkReadReceiver.process(messageIds)
    }
  }
}
