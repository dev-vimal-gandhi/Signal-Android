package com.servalabs.chat.badges.gifts.viewgift.sent

import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.recipients.Recipient

data class ViewSentGiftState(
  val recipient: Recipient? = null,
  val badge: Badge? = null
)
