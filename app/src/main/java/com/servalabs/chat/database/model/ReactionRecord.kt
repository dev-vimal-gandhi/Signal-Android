package com.servalabs.chat.database.model

import com.servalabs.chat.recipients.RecipientId

/**
 * Represents an individual reaction to a message.
 */
data class ReactionRecord(
  val emoji: String,
  val author: RecipientId,
  val dateSent: Long,
  val dateReceived: Long
)
