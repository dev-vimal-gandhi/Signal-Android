package com.servalabs.chat.database.helpers.migration

import android.app.Application
import com.servalabs.chat.database.SQLiteDatabase

@Suppress("ClassName")
object V157_RecipeintHiddenMigration : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("ALTER TABLE recipient ADD COLUMN hidden INTEGER DEFAULT 0")
  }
}
