package com.servalabs.chat.jobs

import org.signal.core.util.logging.Log
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.jobmanager.impl.NetworkConstraint
import com.servalabs.chat.jobmanager.impl.SealedSenderConstraint
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.net.NotPushRegisteredException
import com.servalabs.chat.recipients.Recipient
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException
import org.whispersystems.signalservice.api.push.exceptions.ServerRejectedException

/**
 * Sends a sync message to linked devices to notify them to refresh subscription status.
 */
class MultiDeviceSubscriptionSyncRequestJob private constructor(parameters: Parameters) : BaseJob(parameters) {

  companion object {
    const val KEY = "MultiDeviceSubscriptionSyncRequestJob"

    private val TAG = Log.tag(MultiDeviceSubscriptionSyncRequestJob::class.java)

    @JvmStatic
    fun enqueue() {
      val job = MultiDeviceSubscriptionSyncRequestJob(
        Parameters.Builder()
          .setQueue("MultiDeviceSubscriptionSyncRequestJob")
          .setMaxInstancesForFactory(2)
          .addConstraint(NetworkConstraint.KEY)
          .addConstraint(SealedSenderConstraint.KEY)
          .setMaxAttempts(10)
          .build()
      )

      AppDependencies.jobManager.add(job)
    }
  }

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  override fun onFailure() {
    Log.w(TAG, "Did not succeed!")
  }

  override fun onRun() {
    if (!Recipient.self().isRegistered) {
      throw NotPushRegisteredException()
    }

    if (!SignalStore.account.isMultiDevice) {
      Log.i(TAG, "Not multi device, aborting...")
      return
    }

    val messageSender = AppDependencies.signalServiceMessageSender

    messageSender.sendSyncMessage(SignalServiceSyncMessage.forFetchLatest(SignalServiceSyncMessage.FetchType.SUBSCRIPTION_STATUS))
  }

  override fun onShouldRetry(e: Exception): Boolean {
    return e is PushNetworkException && e !is ServerRejectedException
  }

  class Factory : Job.Factory<MultiDeviceSubscriptionSyncRequestJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): MultiDeviceSubscriptionSyncRequestJob {
      return MultiDeviceSubscriptionSyncRequestJob(parameters)
    }
  }
}
