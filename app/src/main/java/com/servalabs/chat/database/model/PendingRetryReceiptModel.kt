package com.servalabs.chat.database.model

import com.servalabs.chat.recipients.RecipientId

/** A model for [com.servalabs.chat.database.PendingRetryReceiptTable] */
data class PendingRetryReceiptModel(
  val id: Long,
  val author: RecipientId,
  val authorDevice: Int,
  val sentTimestamp: Long,
  val receivedTimestamp: Long,
  val threadId: Long
)
