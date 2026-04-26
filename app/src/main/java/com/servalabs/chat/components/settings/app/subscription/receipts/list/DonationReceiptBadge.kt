package com.servalabs.chat.components.settings.app.subscription.receipts.list

import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.database.model.InAppPaymentReceiptRecord

data class DonationReceiptBadge(
  val type: InAppPaymentReceiptRecord.Type,
  val level: Int,
  val badge: Badge
)
