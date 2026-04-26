package com.servalabs.chat.keyboard.emoji

import com.servalabs.chat.components.emoji.EmojiEventListener
import com.servalabs.chat.keyboard.emoji.search.EmojiSearchFragment

interface EmojiKeyboardCallback :
  EmojiEventListener,
  EmojiKeyboardPageFragment.Callback,
  EmojiSearchFragment.Callback
