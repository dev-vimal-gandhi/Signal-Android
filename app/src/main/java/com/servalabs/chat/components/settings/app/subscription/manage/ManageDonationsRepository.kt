/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.subscription.manage

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.servalabs.chat.database.DatabaseObserver.InAppPaymentObserver
import com.servalabs.chat.database.InAppPaymentTable
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.databaseprotos.InAppPaymentData
import com.servalabs.chat.dependencies.AppDependencies

object ManageDonationsRepository {
  /**
   * Emits any time we see a successfully completed IDEAL payment that we've not notified the user about.
   */
  fun consumeSuccessfulIdealPayments(): Flow<InAppPaymentTable.InAppPayment> {
    return callbackFlow {
      val observer = InAppPaymentObserver {
        if (it.state == InAppPaymentTable.State.END &&
          it.data.error == null &&
          it.data.paymentMethodType == InAppPaymentData.PaymentMethodType.IDEAL &&
          !it.notified
        ) {
          trySendBlocking(it)

          SignalDatabase.inAppPayments.update(
            it.copy(notified = true)
          )
        }
      }

      AppDependencies.databaseObserver.registerInAppPaymentObserver(observer)
      awaitClose { AppDependencies.databaseObserver.unregisterObserver(observer) }
    }
  }
}
