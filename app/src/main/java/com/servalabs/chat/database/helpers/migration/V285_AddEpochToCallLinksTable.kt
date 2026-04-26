/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.database.helpers.migration

import android.app.Application
import com.servalabs.chat.database.SQLiteDatabase

@Suppress("ClassName")
object V285_AddEpochToCallLinksTable : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("ALTER TABLE call_link ADD COLUMN epoch BLOB DEFAULT NULL")
  }
}
