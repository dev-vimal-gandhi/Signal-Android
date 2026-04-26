package com.servalabs.chat.database.model

import com.servalabs.chat.recipients.RecipientId

data class DistributionListPartialRecord(
  val id: DistributionListId,
  val name: CharSequence,
  val recipientId: RecipientId,
  val allowsReplies: Boolean,
  val isUnknown: Boolean,
  val privacyMode: DistributionListPrivacyMode
)
