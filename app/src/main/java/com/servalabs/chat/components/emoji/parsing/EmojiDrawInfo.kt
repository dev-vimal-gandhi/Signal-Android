package com.servalabs.chat.components.emoji.parsing

import com.servalabs.chat.emoji.EmojiPage

data class EmojiDrawInfo(val page: EmojiPage, val index: Int, val emoji: String, val rawEmoji: String?, val jumboSheet: String?)
