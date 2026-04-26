package com.servalabs.chat.jobs

import androidx.core.content.ContextCompat
import org.signal.core.util.logging.Log
import com.servalabs.chat.R
import com.servalabs.chat.avatar.Avatar
import com.servalabs.chat.avatar.AvatarRenderer
import com.servalabs.chat.avatar.Avatars
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.profiles.AvatarHelper
import com.servalabs.chat.profiles.ProfileName
import com.servalabs.chat.providers.BlobProvider
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.transport.RetryLaterException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Creates the Release Channel (Signal) recipient.
 */
class CreateReleaseChannelJob private constructor(parameters: Parameters) : BaseJob(parameters) {
  companion object {
    const val KEY = "CreateReleaseChannelJob"

    private val TAG = Log.tag(CreateReleaseChannelJob::class.java)

    fun create(): CreateReleaseChannelJob {
      return CreateReleaseChannelJob(
        Parameters.Builder()
          .setQueue("CreateReleaseChannelJob")
          .setMaxInstancesForFactory(1)
          .setMaxAttempts(3)
          .build()
      )
    }
  }

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  override fun onFailure() = Unit

  override fun onRun() {
    if (!SignalStore.account.isRegistered) {
      Log.i(TAG, "Not registered, skipping.")
      return
    }

    if (SignalStore.releaseChannel.releaseChannelRecipientId != null) {
      val existingId = SignalStore.releaseChannel.releaseChannelRecipientId!!
      val recipient = Recipient.resolved(existingId)

      if (recipient.hasServiceId || recipient.hasE164 || recipient.isGroup || recipient.isDistributionList || recipient.isCallLink) {
        Log.w(TAG, "Release channel recipient $existingId is not a valid release channel recipient (hasServiceId: ${recipient.hasServiceId}, hasE164: ${recipient.hasE164}, isGroup: ${recipient.isGroup}, isDistributionList: ${recipient.isDistributionList}, isCallLink: ${recipient.isCallLink}). Clearing and recreating.")
        SignalStore.releaseChannel.clearReleaseChannelRecipientId()
      } else {
        Log.i(TAG, "Already created Release Channel recipient $existingId")
        if (recipient.profileAvatar.isNullOrEmpty() || !SignalStore.releaseChannel.hasUpdatedAvatar) {
          SignalStore.releaseChannel.hasUpdatedAvatar = true
          setAvatar(recipient.id)
        }
        return
      }
    }

    val recipients = SignalDatabase.recipients

    val releaseChannelId: RecipientId = recipients.insertReleaseChannelRecipient()
    SignalStore.releaseChannel.setReleaseChannelRecipientId(releaseChannelId)
    SignalStore.releaseChannel.hasUpdatedAvatar = true

    recipients.setProfileName(releaseChannelId, ProfileName.asGiven("Signal"))
    recipients.setMuted(releaseChannelId, Long.MAX_VALUE)
    setAvatar(releaseChannelId)
  }

  private fun setAvatar(id: RecipientId) {
    val latch = CountDownLatch(1)
    AvatarRenderer.renderAvatar(
      context,
      Avatar.Resource(
        R.drawable.ic_signal_logo_large,
        Avatars.ColorPair(ContextCompat.getColor(context, R.color.notification_background_ultramarine), ContextCompat.getColor(context, R.color.core_white), "")
      ),
      onAvatarRendered = { media ->
        AvatarHelper.setAvatar(context, id, BlobProvider.getInstance().getStream(context, media.uri))
        SignalDatabase.recipients.setProfileAvatar(id, "local")
        latch.countDown()
      },
      onRenderFailed = { t ->
        Log.w(TAG, t)
        latch.countDown()
      }
    )

    try {
      val completed: Boolean = latch.await(30, TimeUnit.SECONDS)
      if (!completed) {
        throw RetryLaterException()
      }
    } catch (e: InterruptedException) {
      throw RetryLaterException()
    }
  }

  override fun onShouldRetry(e: Exception): Boolean = e is RetryLaterException

  class Factory : Job.Factory<CreateReleaseChannelJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): CreateReleaseChannelJob {
      return CreateReleaseChannelJob(parameters)
    }
  }
}
