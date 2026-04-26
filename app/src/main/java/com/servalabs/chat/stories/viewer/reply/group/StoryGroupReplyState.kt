package com.servalabs.chat.stories.viewer.reply.group

import com.servalabs.chat.conversation.colors.NameColor
import com.servalabs.chat.recipients.RecipientId

data class StoryGroupReplyState(
  val threadId: Long = 0L,
  val replies: List<ReplyBody> = emptyList(),
  val nameColors: Map<RecipientId, NameColor> = emptyMap(),
  val loadState: LoadState = LoadState.INIT
) {
  val noReplies: Boolean = replies.isEmpty()

  enum class LoadState {
    INIT,
    READY
  }
}
