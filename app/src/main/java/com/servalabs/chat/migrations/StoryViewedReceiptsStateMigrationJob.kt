package com.servalabs.chat.migrations

import com.servalabs.chat.database.SignalDatabase.Companion.recipients
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.storage.StorageSyncHelper
import com.servalabs.chat.util.TextSecurePreferences

/**
 * Added as a way to initialize the story viewed receipts setting.
 */
internal class StoryViewedReceiptsStateMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {
  companion object {
    const val KEY = "StoryViewedReceiptsStateMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (!SignalStore.story.isViewedReceiptsStateSet()) {
      SignalStore.story.viewedReceiptsEnabled = TextSecurePreferences.isReadReceiptsEnabled(context)
      if (SignalStore.account.isRegistered) {
        recipients.markNeedsSync(Recipient.self().id)
        StorageSyncHelper.scheduleSyncForDataChange()
      }
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<StoryViewedReceiptsStateMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): StoryViewedReceiptsStateMigrationJob {
      return StoryViewedReceiptsStateMigrationJob(parameters)
    }
  }
}
