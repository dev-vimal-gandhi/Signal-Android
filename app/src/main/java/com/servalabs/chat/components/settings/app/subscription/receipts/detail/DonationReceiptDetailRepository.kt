package com.servalabs.chat.components.settings.app.subscription.receipts.detail

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.InAppPaymentReceiptRecord

class DonationReceiptDetailRepository {
  fun getDonationReceiptRecord(id: Long): Single<InAppPaymentReceiptRecord> {
    return Single.fromCallable<InAppPaymentReceiptRecord> {
      SignalDatabase.donationReceipts.getReceipt(id)!!
    }.subscribeOn(Schedulers.io())
  }
}
