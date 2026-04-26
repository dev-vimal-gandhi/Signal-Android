package com.servalabs.chat.avatar.picker

import com.servalabs.chat.avatar.Avatar

data class AvatarPickerState(
  val currentAvatar: Avatar? = null,
  val selectableAvatars: List<Avatar> = listOf(),
  val canSave: Boolean = false,
  val canClear: Boolean = false,
  val isCleared: Boolean = false
)
