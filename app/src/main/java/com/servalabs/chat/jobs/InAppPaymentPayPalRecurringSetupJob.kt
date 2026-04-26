/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.jobs

import android.annotation.SuppressLint
import org.signal.donations.PaymentSource
import com.servalabs.chat.components.settings.app.subscription.InAppPaymentsRepository
import com.servalabs.chat.components.settings.app.subscription.InAppPaymentsRepository.requireSubscriberType
import com.servalabs.chat.components.settings.app.subscription.PayPalRepository
import com.servalabs.chat.components.settings.app.subscription.RecurringInAppPaymentRepository
import com.servalabs.chat.database.InAppPaymentTable
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.jobs.protos.InAppPaymentSetupJobData

class InAppPaymentPayPalRecurringSetupJob private constructor(data: InAppPaymentSetupJobData, parameters: Parameters) : InAppPaymentSetupJob(data, parameters) {

  companion object {
    const val KEY = "InAppPaymentPayPalRecurringSetupJob"

    /**
     * Creates a new job for performing stripe recurring payment setup. Note that
     * we do not require network for this job, as if the network is not present, we
     * should treat that as an immediate error and fail the job.
     */
    fun create(
      inAppPayment: InAppPaymentTable.InAppPayment,
      paymentSource: PaymentSource
    ): InAppPaymentPayPalRecurringSetupJob {
      return InAppPaymentPayPalRecurringSetupJob(
        getJobData(inAppPayment, paymentSource),
        getParameters(inAppPayment)
      )
    }
  }

  private val payPalRepository = PayPalRepository(AppDependencies.donationsService)

  override fun performPreUserAction(inAppPayment: InAppPaymentTable.InAppPayment): RequiredUserAction {
    info("Ensuring the subscriber id is set on the server.")
    RecurringInAppPaymentRepository.ensureSubscriberIdSync(inAppPayment.type.requireSubscriberType())
    info("Canceling active subscription (if necessary).")
    RecurringInAppPaymentRepository.cancelActiveSubscriptionIfNecessarySync(inAppPayment.type.requireSubscriberType())
    info("Creating payment method")
    val response = payPalRepository.createPaymentMethod(inAppPayment.type.requireSubscriberType())
    return RequiredUserAction.PayPalActionRequired(
      approvalUrl = response.approvalUrl,
      tokenOrPaymentId = response.token
    )
  }

  @SuppressLint("CheckResult")
  override fun performPostUserAction(inAppPayment: InAppPaymentTable.InAppPayment): Result {
    val paymentMethodId = inAppPayment.data.payPalActionComplete!!.paymentId
    info("Setting default payment method.")
    payPalRepository.setDefaultPaymentMethod(inAppPayment.type.requireSubscriberType(), paymentMethodId)
    info("Setting subscription level.")
    RecurringInAppPaymentRepository.setSubscriptionLevelSync(inAppPayment)

    return Result.success()
  }

  override fun getFactoryKey(): String = KEY

  override fun run(): Result {
    return synchronized(InAppPaymentsRepository.resolveLock(InAppPaymentTable.InAppPaymentId(data.inAppPaymentId))) {
      performTransaction()
    }
  }

  class Factory : Job.Factory<InAppPaymentPayPalRecurringSetupJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): InAppPaymentPayPalRecurringSetupJob {
      val data = serializedData?.let { InAppPaymentSetupJobData.ADAPTER.decode(it) } ?: error("Missing job data!")

      return InAppPaymentPayPalRecurringSetupJob(data, parameters)
    }
  }
}
