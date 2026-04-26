package com.servalabs.chat.components.settings.app.subscription.manage

import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.database.InAppPaymentTable
import com.servalabs.chat.database.model.databaseprotos.PendingOneTimeDonation
import com.servalabs.chat.subscription.Subscription

data class ManageDonationsState(
  val hasOneTimeBadge: Boolean = false,
  val hasReceipts: Boolean = false,
  val featuredBadge: Badge? = null,
  val isLoaded: Boolean = false,
  val networkError: Boolean = false,
  val availableSubscriptions: List<Subscription> = emptyList(),
  val activeSubscription: InAppPaymentTable.InAppPayment? = null,
  val subscriptionRedemptionState: RedemptionState = RedemptionState.NONE,
  val pendingOneTimeDonation: PendingOneTimeDonation? = null,
  val nonVerifiedMonthlyDonation: NonVerifiedMonthlyDonation? = null,
  val subscriberRequiresCancel: Boolean = false
) {

  enum class RedemptionState {
    NONE,
    IN_PROGRESS,
    SUBSCRIPTION_REFRESH,
    IS_PENDING_BANK_TRANSFER,
    FAILED
  }
}
