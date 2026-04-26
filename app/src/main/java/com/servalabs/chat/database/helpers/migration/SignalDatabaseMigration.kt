package com.servalabs.chat.database.helpers.migration

import android.app.Application
import com.servalabs.chat.database.SQLiteDatabase

/**
 * Simple interface for allowing database migrations to live outside of [com.servalabs.chat.database.helpers.SignalDatabaseMigrations].
 */
interface SignalDatabaseMigration {
  /** True if you want foreign key constraints to be enforced during a migration, otherwise false. Defaults to false. */
  val enableForeignKeys: Boolean
    get() = false

  fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
}
