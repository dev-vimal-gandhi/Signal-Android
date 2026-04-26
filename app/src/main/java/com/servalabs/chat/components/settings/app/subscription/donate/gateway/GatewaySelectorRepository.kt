package com.servalabs.chat.components.settings.app.subscription.donate.gateway

import io.reactivex.rxjava3.core.Single
import org.signal.core.util.money.FiatMoney
import com.servalabs.chat.components.settings.app.subscription.InAppPaymentsRepository
import com.servalabs.chat.components.settings.app.subscription.getAvailablePaymentMethods
import com.servalabs.chat.database.InAppPaymentTable
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.databaseprotos.InAppPaymentData
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.payments.currency.CurrencyUtil
import org.whispersystems.signalservice.internal.push.SubscriptionsConfiguration
import java.util.Locale

object GatewaySelectorRepository {
  fun getAvailableGatewayConfiguration(currencyCode: String): Single<GatewayConfiguration> {
    return Single.fromCallable {
      AppDependencies.donationsService.getDonationsConfiguration(Locale.getDefault())
    }.flatMap { it.flattenResult() }
      .map { configuration ->
        val available = configuration.getAvailablePaymentMethods(currencyCode).map {
          when (it) {
            SubscriptionsConfiguration.PAYPAL -> listOf(InAppPaymentData.PaymentMethodType.PAYPAL)
            SubscriptionsConfiguration.CARD -> listOf(InAppPaymentData.PaymentMethodType.CARD, InAppPaymentData.PaymentMethodType.GOOGLE_PAY)
            SubscriptionsConfiguration.SEPA_DEBIT -> listOf(InAppPaymentData.PaymentMethodType.SEPA_DEBIT)
            SubscriptionsConfiguration.IDEAL -> listOf(InAppPaymentData.PaymentMethodType.IDEAL)
            else -> listOf()
          }
        }.flatten().toSet()

        GatewayConfiguration(
          availableGateways = available,
          sepaEuroMaximum = if (configuration.sepaMaximumEuros != null) FiatMoney(configuration.sepaMaximumEuros, CurrencyUtil.EURO) else null
        )
      }
  }

  fun setInAppPaymentMethodType(inAppPayment: InAppPaymentTable.InAppPayment, paymentMethodType: InAppPaymentData.PaymentMethodType): Single<InAppPaymentTable.InAppPayment> {
    return Single.fromCallable {
      SignalDatabase.inAppPayments.update(
        inAppPayment.copy(
          data = inAppPayment.data.copy(
            paymentMethodType = paymentMethodType
          )
        )
      )
    }.flatMap { InAppPaymentsRepository.requireInAppPayment(inAppPayment.id) }
  }

  data class GatewayConfiguration(
    val availableGateways: Set<InAppPaymentData.PaymentMethodType>,
    val sepaEuroMaximum: FiatMoney?
  )
}
