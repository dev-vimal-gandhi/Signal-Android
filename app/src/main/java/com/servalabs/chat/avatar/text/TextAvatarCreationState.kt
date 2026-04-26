package com.servalabs.chat.avatar.text

import com.servalabs.chat.avatar.Avatar
import com.servalabs.chat.avatar.AvatarColorItem
import com.servalabs.chat.avatar.Avatars

data class TextAvatarCreationState(
  val currentAvatar: Avatar.Text
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
