/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.database.helpers.migration

import android.app.Application
import com.servalabs.chat.database.SQLiteDatabase

/**
 * Adds timestamp index to call table to speed up queries.
 */
@Suppress("ClassName")
object V255_AddCallTableLogIndex : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("CREATE INDEX IF NOT EXISTS call_log_index ON call (timestamp, peer, event, type, deletion_timestamp)")
  }
}
