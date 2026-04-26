package com.servalabs.chat.stories.viewer.reply.direct

import com.servalabs.chat.database.model.MessageRecord
import com.servalabs.chat.recipients.Recipient

data class StoryDirectReplyState(
  val groupDirectReplyRecipient: Recipient? = null,
  val storyRecord: MessageRecord? = null
)
