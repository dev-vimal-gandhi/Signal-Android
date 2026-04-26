package com.servalabs.chat.keyboard.emoji

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import com.servalabs.chat.components.emoji.EmojiPageModel
import com.servalabs.chat.components.emoji.RecentEmojiPageModel
import com.servalabs.chat.emoji.EmojiSource.Companion.latest
import com.servalabs.chat.util.TextSecurePreferences
import java.util.function.Consumer

class EmojiKeyboardPageRepository(private val context: Context) {
  fun getEmoji(consumer: Consumer<List<EmojiPageModel>>) {
    SignalExecutors.BOUNDED.execute {
      val list = mutableListOf<EmojiPageModel>()
      list += RecentEmojiPageModel(context, TextSecurePreferences.RECENT_STORAGE_KEY)
      list += latest.displayPages
      consumer.accept(list)
    }
  }
}
