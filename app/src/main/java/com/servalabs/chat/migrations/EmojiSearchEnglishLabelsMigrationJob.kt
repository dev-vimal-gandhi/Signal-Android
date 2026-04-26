/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.migrations

import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob
import com.servalabs.chat.keyvalue.SignalStore

/**
 * Schedules job to download both the localized and English emoji search indices, ensuring that emoji search data is available in the user's preferred
 * language as well as English.
 */
internal class EmojiSearchEnglishLabelsMigrationJob(parameters: Parameters = Parameters.Builder().build()) : MigrationJob(parameters) {
  companion object {
    const val KEY = "EmojiSearchEnglishLabelsMigrationJob"
  }

  override fun getFactoryKey(): String = KEY
  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (EmojiSearchIndexDownloadJob.LANGUAGE_CODE_ENGLISH != SignalStore.emoji.searchLanguage) {
      SignalStore.emoji.clearSearchIndexMetadata()
      EmojiSearchIndexDownloadJob.scheduleImmediately()
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<EmojiSearchEnglishLabelsMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): EmojiSearchEnglishLabelsMigrationJob {
      return EmojiSearchEnglishLabelsMigrationJob(parameters)
    }
  }
}
