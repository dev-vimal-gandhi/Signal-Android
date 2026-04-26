package com.servalabs.chat.database

internal interface ThreadIdDatabaseReference {
  fun remapThread(fromId: Long, toId: Long)
}
