/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package com.servalabs.chat.conversation

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.signal.core.models.UriSerializer
import org.signal.core.models.media.Media
import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.conversation.ConversationIntents.ConversationScreenType
import com.servalabs.chat.mms.SlideFactory
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.stickers.StickerLocator

@Serializable
@Parcelize
data class ConversationArgs(
  val recipientId: RecipientId,
  @JvmField val threadId: Long,
  val draftText: String?,
  @Serializable(with = UriSerializer::class) val draftMedia: Uri?,
  val draftContentType: String?,
  val media: List<Media?>?,
  val stickerLocator: StickerLocator?,
  val isBorderless: Boolean,
  val distributionType: Int,
  val startingPosition: Int,
  val isFirstTimeInSelfCreatedGroup: Boolean,
  val isWithSearchOpen: Boolean,
  val giftBadge: Badge?,
  val shareDataTimestamp: Long,
  val conversationScreenType: ConversationScreenType,
  val isIncognito: Boolean = false,
  val hasWallpaper: Boolean = false
) : Parcelable {
  @IgnoredOnParcel
  val draftMediaType: SlideFactory.MediaType? = SlideFactory.MediaType.from(draftContentType)

  fun canInitializeFromDatabase(): Boolean {
    return draftText == null && (draftMedia == null || ConversationIntents.isBubbleIntentUri(draftMedia) || ConversationIntents.isNotificationIntentUri(draftMedia)) && draftMediaType == null
  }
}
