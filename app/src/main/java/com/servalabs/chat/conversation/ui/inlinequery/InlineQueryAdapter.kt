package com.servalabs.chat.conversation.ui.inlinequery

import com.servalabs.chat.R
import com.servalabs.chat.util.adapter.mapping.AnyMappingModel
import com.servalabs.chat.util.adapter.mapping.MappingAdapter

class InlineQueryAdapter(listener: (AnyMappingModel) -> Unit) : MappingAdapter() {
  init {
    registerFactory(InlineQueryEmojiResult.Model::class.java, { InlineQueryEmojiResult.ViewHolder(it, listener) }, R.layout.inline_query_emoji_result)
  }
}
