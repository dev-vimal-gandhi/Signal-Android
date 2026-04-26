package com.servalabs.chat.stories.settings.group

import com.servalabs.chat.recipients.Recipient

data class GroupStorySettingsState(
  val name: String = "",
  val members: List<Recipient> = emptyList(),
  val removed: Boolean = false
)
