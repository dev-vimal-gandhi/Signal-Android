package com.servalabs.chat.jobs

import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEmpty
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.signal.core.util.deleteAll
import org.signal.core.util.money.FiatMoney
import org.signal.donations.InAppPaymentType
import com.servalabs.chat.components.settings.app.subscription.DonationSerializationHelper.toFiatValue
import com.servalabs.chat.database.DonationReceiptTable
import com.servalabs.chat.database.InAppPaymentTable
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.InAppPaymentReceiptRecord
import com.servalabs.chat.database.model.databaseprotos.InAppPaymentData
import com.servalabs.chat.testing.SignalActivityRule
import java.math.BigDecimal
import java.util.Currency

@RunWith(AndroidJUnit4::class)
class InAppPaymentAuthCheckJobTest {

  companion object {
    private const val TEST_INTENT_ID = "test-intent-id"
    private const val TEST_CLIENT_SECRET = "test-client-secret"
  }

  @get:Rule
  val harness = SignalActivityRule()

  @Before
  fun setUp() {
    SignalDatabase.inAppPayments.writableDatabase.deleteAll(InAppPaymentTable.TABLE_NAME)
    SignalDatabase.donationReceipts.writableDatabase.deleteAll(DonationReceiptTable.TABLE_NAME)
  }

  @Test
  fun givenCanceledOneTimeAuthRequiredPayment_whenICheck_thenIDoNotExpectAReceipt() {
    SignalDatabase.inAppPayments.insert(
      type = InAppPaymentType.ONE_TIME_DONATION,
      state = InAppPaymentTable.State.WAITING_FOR_AUTHORIZATION,
      subscriberId = null,
      endOfPeriod = null,
      inAppPaymentData = InAppPaymentData(
        amount = FiatMoney(BigDecimal.ONE, Currency.getInstance("USD")).toFiatValue(),
        waitForAuth = InAppPaymentData.WaitingForAuthorizationState(
          stripeIntentId = TEST_INTENT_ID,
          stripeClientSecret = TEST_CLIENT_SECRET
        )
      )
    )

    InAppPaymentAuthCheckJob().run()

    val receipts = SignalDatabase.donationReceipts.getReceipts(InAppPaymentReceiptRecord.Type.ONE_TIME_DONATION)
    assertThat(receipts).isEmpty()
  }
}
