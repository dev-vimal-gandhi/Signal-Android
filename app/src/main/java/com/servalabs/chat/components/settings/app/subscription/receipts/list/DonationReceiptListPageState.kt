package com.servalabs.chat.components.settings.app.subscription.receipts.list

import com.servalabs.chat.database.model.InAppPaymentReceiptRecord

data class DonationReceiptListPageState(
  val records: List<InAppPaymentReceiptRecord> = emptyList(),
  val isLoaded: Boolean = false
)
