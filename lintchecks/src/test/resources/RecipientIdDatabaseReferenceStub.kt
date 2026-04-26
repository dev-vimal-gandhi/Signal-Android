package com.servalabs.chat.database

internal interface RecipientIdDatabaseReference {
  fun remapRecipient(fromId: RecipientId?, toId: RecipientId?)
}
