package com.servalabs.chat.database.helpers.migration

import android.app.Application
import com.servalabs.chat.database.SQLiteDatabase

@Suppress("ClassName")
object V164_ThreadDatabaseReadIndexMigration : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("CREATE INDEX IF NOT EXISTS thread_read ON thread (read);")
  }
}
