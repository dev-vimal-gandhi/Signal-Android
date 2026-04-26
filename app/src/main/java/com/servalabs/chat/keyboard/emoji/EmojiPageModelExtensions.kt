package com.servalabs.chat.keyboard.emoji

import com.servalabs.chat.components.emoji.EmojiPageModel
import com.servalabs.chat.components.emoji.EmojiPageViewGridAdapter
import com.servalabs.chat.components.emoji.RecentEmojiPageModel
import com.servalabs.chat.components.emoji.parsing.EmojiTree
import com.servalabs.chat.emoji.EmojiCategory
import com.servalabs.chat.emoji.EmojiSource
import com.servalabs.chat.util.adapter.mapping.MappingModel

fun EmojiPageModel.toMappingModels(): List<MappingModel<*>> {
  val emojiTree: EmojiTree = EmojiSource.latest.emojiTree

  return displayEmoji.map {
    val isTextEmoji = EmojiCategory.EMOTICONS.key == key || (RecentEmojiPageModel.KEY == key && emojiTree.getEmoji(it.value, 0, it.value.length) == null)

    if (isTextEmoji) {
      EmojiPageViewGridAdapter.EmojiTextModel(key, it)
    } else {
      EmojiPageViewGridAdapter.EmojiModel(key, it)
    }
  }
}
