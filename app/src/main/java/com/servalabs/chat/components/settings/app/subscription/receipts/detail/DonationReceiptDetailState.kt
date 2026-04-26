package com.servalabs.chat.components.settings.app.subscription.receipts.detail

import com.servalabs.chat.database.model.InAppPaymentReceiptRecord

data class DonationReceiptDetailState(
  val inAppPaymentReceiptRecord: InAppPaymentReceiptRecord? = null
)
