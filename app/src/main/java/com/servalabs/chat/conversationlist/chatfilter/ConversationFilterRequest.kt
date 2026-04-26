package com.servalabs.chat.conversationlist.chatfilter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.servalabs.chat.conversationlist.model.ConversationFilter

@Parcelize
data class ConversationFilterRequest(
  val filter: ConversationFilter,
  val source: ConversationFilterSource
) : Parcelable
