package com.servalabs.chat.conversation.v2

import android.content.Context
import org.signal.core.models.ServiceId
import com.servalabs.chat.crypto.ProfileKeyUtil
import com.servalabs.chat.database.MessageTable
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.GroupRecord
import com.servalabs.chat.database.model.MessageId
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.groups.GroupAccessControl
import com.servalabs.chat.groups.GroupNotAMemberException
import com.servalabs.chat.messages.GroupSendUtil
import com.servalabs.chat.mms.OutgoingMessage
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.transport.UndeliverableMessageException
import com.servalabs.chat.util.GroupUtil
import org.whispersystems.signalservice.api.crypto.ContentHint
import org.whispersystems.signalservice.api.messages.SendMessageResult
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage.Companion.newBuilder
import java.io.IOException
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Functions used when pinning/unpinning messages
 */
object PinSendUtil {

  private val PIN_TERMINATE_TIMEOUT = 7000.milliseconds

  @Throws(IOException::class, GroupNotAMemberException::class, UndeliverableMessageException::class)
  fun sendPinMessage(applicationContext: Context, threadRecipient: Recipient, message: OutgoingMessage, destinations: List<Recipient>, includeSelf: Boolean, relatedMessageId: Long): List<SendMessageResult?> {
    val builder = newBuilder()
    val groupId = if (threadRecipient.isPushV2Group) threadRecipient.requireGroupId().requireV2() else null

    if (groupId != null) {
      val groupRecord: GroupRecord? = SignalDatabase.groups.getGroup(groupId).getOrNull()

      if (groupRecord != null && !groupRecord.isActive) {
        throw UndeliverableMessageException("Cannot pin messages in an inactive group!")
      }

      if (groupRecord != null && groupRecord.attributesAccessControl == GroupAccessControl.ONLY_ADMINS && !groupRecord.isAdmin(Recipient.self())) {
        throw UndeliverableMessageException("Non-admins cannot pin messages!")
      }
      GroupUtil.setDataMessageGroupContext(AppDependencies.application, builder, groupId)
    }

    val sentTime = System.currentTimeMillis()
    val message = builder
      .withTimestamp(sentTime)
      .withExpiration((message.expiresIn / 1000).toInt())
      .withProfileKey(ProfileKeyUtil.getSelfProfileKey().serialize())
      .withPinnedMessage(
        SignalServiceDataMessage.PinnedMessage(
          targetAuthor = ServiceId.parseOrThrow(message.messageExtras!!.pinnedMessage!!.targetAuthorAci),
          targetSentTimestamp = message.messageExtras.pinnedMessage.targetTimestamp,
          pinDurationInSeconds = message.messageExtras.pinnedMessage.pinDurationInSeconds.takeIf { it != MessageTable.PIN_FOREVER }?.toInt(),
          forever = (message.messageExtras.pinnedMessage.pinDurationInSeconds == MessageTable.PIN_FOREVER).takeIf { it }
        )
      )
      .build()

    return if (includeSelf) {
      listOf(AppDependencies.signalServiceMessageSender.sendSyncMessage(message))
    } else {
      GroupSendUtil.sendResendableDataMessage(
        applicationContext,
        groupId,
        null,
        destinations,
        false,
        ContentHint.RESENDABLE,
        MessageId(relatedMessageId),
        message,
        false,
        false,
        null
      ) { System.currentTimeMillis() - sentTime > PIN_TERMINATE_TIMEOUT.inWholeMilliseconds }
    }
  }

  @Throws(IOException::class, GroupNotAMemberException::class, UndeliverableMessageException::class)
  fun sendUnpinMessage(applicationContext: Context, threadRecipient: Recipient, targetAuthor: ServiceId, targetSentTimestamp: Long, destinations: List<Recipient>, includeSelf: Boolean, relatedMessageId: Long): List<SendMessageResult?> {
    val builder = newBuilder()
    val groupId = if (threadRecipient.isPushV2Group) threadRecipient.requireGroupId().requireV2() else null
    if (groupId != null) {
      val groupRecord: GroupRecord? = SignalDatabase.groups.getGroup(groupId).getOrNull()

      if (groupRecord != null && !groupRecord.isActive) {
        throw UndeliverableMessageException("Cannot unpin messages in an inactive group!")
      }

      if (groupRecord != null && groupRecord.attributesAccessControl == GroupAccessControl.ONLY_ADMINS && !groupRecord.isAdmin(Recipient.self())) {
        throw UndeliverableMessageException("Non-admins cannot pin messages!")
      }

      GroupUtil.setDataMessageGroupContext(AppDependencies.application, builder, groupId)
    }

    val sentTime = System.currentTimeMillis()
    val message = builder
      .withTimestamp(sentTime)
      .withProfileKey(ProfileKeyUtil.getSelfProfileKey().serialize())
      .withUnpinnedMessage(
        SignalServiceDataMessage.UnpinnedMessage(
          targetAuthor = targetAuthor,
          targetSentTimestamp = targetSentTimestamp
        )
      )
      .build()

    return if (includeSelf) {
      listOf(AppDependencies.signalServiceMessageSender.sendSyncMessage(message))
    } else {
      GroupSendUtil.sendResendableDataMessage(
        applicationContext,
        groupId,
        null,
        destinations,
        false,
        ContentHint.RESENDABLE,
        MessageId(relatedMessageId),
        message,
        false,
        false,
        null
      ) { System.currentTimeMillis() - sentTime > PIN_TERMINATE_TIMEOUT.inWholeMilliseconds }
    }
  }
}
