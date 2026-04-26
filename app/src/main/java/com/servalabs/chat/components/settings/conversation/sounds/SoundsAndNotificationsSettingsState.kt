package com.servalabs.chat.components.settings.conversation.sounds

import com.servalabs.chat.database.RecipientTable
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.recipients.RecipientId

data class SoundsAndNotificationsSettingsState(
  val recipientId: RecipientId = Recipient.UNKNOWN.id,
  val muteUntil: Long = 0L,
  val mentionSetting: RecipientTable.NotificationSetting = RecipientTable.NotificationSetting.DO_NOT_NOTIFY,
  val hasCustomNotificationSettings: Boolean = false,
  val hasMentionsSupport: Boolean = false,
  val channelConsistencyCheckComplete: Boolean = false
)
