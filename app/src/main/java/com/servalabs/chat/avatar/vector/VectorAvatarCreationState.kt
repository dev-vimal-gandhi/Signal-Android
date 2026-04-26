package com.servalabs.chat.avatar.vector

import com.servalabs.chat.avatar.Avatar
import com.servalabs.chat.avatar.AvatarColorItem
import com.servalabs.chat.avatar.Avatars

data class VectorAvatarCreationState(
  val currentAvatar: Avatar.Vector
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
