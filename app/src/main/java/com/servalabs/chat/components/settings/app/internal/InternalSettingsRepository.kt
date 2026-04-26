package com.servalabs.chat.components.settings.app.internal

import android.content.Context
import org.json.JSONObject
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.donations.InAppPaymentType
import com.servalabs.chat.database.MessageTable
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.RemoteMegaphoneRecord
import com.servalabs.chat.database.model.addButton
import com.servalabs.chat.database.model.addStyle
import com.servalabs.chat.database.model.databaseprotos.BodyRangeList
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.emoji.EmojiFiles
import com.servalabs.chat.jobs.AttachmentDownloadJob
import com.servalabs.chat.jobs.CreateReleaseChannelJob
import com.servalabs.chat.jobs.FetchRemoteMegaphoneImageJob
import com.servalabs.chat.jobs.InAppPaymentRecurringContextJob
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.notifications.v2.ConversationId
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.releasechannel.ReleaseChannel
import java.util.UUID
import kotlin.time.Duration.Companion.days

class InternalSettingsRepository(context: Context) {

  private val context = context.applicationContext

  fun getEmojiVersionInfo(consumer: (EmojiFiles.Version?) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      consumer(EmojiFiles.Version.readVersion(context))
    }
  }

  fun enqueueSubscriptionRedemption() {
    SignalExecutors.BOUNDED.execute {
      val latest = SignalDatabase.inAppPayments.getByLatestEndOfPeriod(InAppPaymentType.RECURRING_DONATION)
      if (latest != null) {
        InAppPaymentRecurringContextJob.createJobChain(latest).enqueue()
      }
    }
  }

  fun addSampleReleaseNote(callToAction: String) {
    SignalExecutors.UNBOUNDED.execute {
      AppDependencies.jobManager.runSynchronously(CreateReleaseChannelJob.create(), 5000)

      val title = "Release Note Title"
      val bodyText = "Release note body. Aren't I awesome?"
      val body = "$title\n\n$bodyText"
      val bodyRangeList = BodyRangeList.Builder()
        .addStyle(BodyRangeList.BodyRange.Style.BOLD, 0, title.length)

      bodyRangeList.addButton("Call to Action Text", callToAction, body.lastIndex, 0)

      val recipientId = SignalStore.releaseChannel.releaseChannelRecipientId!!
      val threadId = SignalDatabase.threads.getOrCreateThreadIdFor(Recipient.resolved(recipientId))

      val insertResult: MessageTable.InsertResult? = ReleaseChannel.insertReleaseChannelMessage(
        recipientId = recipientId,
        body = body,
        threadId = threadId,
        messageRanges = bodyRangeList.build(),
        media = "/static/release-notes/signal.png",
        mediaWidth = 1800,
        mediaHeight = 720
      )

      SignalDatabase.messages.insertBoostRequestMessage(recipientId, threadId)

      if (insertResult != null) {
        SignalDatabase.attachments.getAttachmentsForMessage(insertResult.messageId)
          .forEach { AppDependencies.jobManager.add(AttachmentDownloadJob(insertResult.messageId, it.attachmentId, false)) }

        AppDependencies.messageNotifier.updateNotification(context, ConversationId.forConversation(insertResult.threadId))
      }
    }
  }

  fun addRemoteMegaphone(actionId: RemoteMegaphoneRecord.ActionId) {
    SignalExecutors.UNBOUNDED.execute {
      val record = RemoteMegaphoneRecord(
        uuid = UUID.randomUUID().toString(),
        priority = 100,
        countries = "*:1000000",
        minimumVersion = 1,
        doNotShowBefore = System.currentTimeMillis() - 2.days.inWholeMilliseconds,
        doNotShowAfter = System.currentTimeMillis() + 28.days.inWholeMilliseconds,
        showForNumberOfDays = 30,
        conditionalId = null,
        primaryActionId = actionId,
        secondaryActionId = RemoteMegaphoneRecord.ActionId.SNOOZE,
        imageUrl = "/static/release-notes/donate-heart.png",
        title = "Donate Test",
        body = "Donate body test.",
        primaryActionText = "Donate",
        secondaryActionText = "Snooze",
        primaryActionData = null,
        secondaryActionData = JSONObject("{ \"snoozeDurationDays\": [5, 7, 100] }")
      )

      SignalDatabase.remoteMegaphones.insert(record)

      if (record.imageUrl != null) {
        AppDependencies.jobManager.add(FetchRemoteMegaphoneImageJob(record.uuid, record.imageUrl))
      }
    }
  }
}
