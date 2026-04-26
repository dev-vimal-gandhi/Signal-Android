/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.database.helpers.migration

import android.app.Application
import com.servalabs.chat.database.SQLiteDatabase

/**
 * Drop the unnecessary thread-date index.
 */
@Suppress("ClassName")
object V202_DropMessageTableThreadDateIndex : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP INDEX IF EXISTS message_thread_date_index")
  }
}
