package com.servalabs.chat.stories.viewer.views

import com.servalabs.chat.recipients.Recipient

data class StoryViewItemData(
  val recipient: Recipient,
  val timeViewedInMillis: Long
)
