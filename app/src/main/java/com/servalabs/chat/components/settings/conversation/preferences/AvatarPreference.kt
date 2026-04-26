package com.servalabs.chat.components.settings.conversation.preferences

import android.view.View
import androidx.core.view.ViewCompat
import com.servalabs.chat.R
import com.servalabs.chat.avatar.view.AvatarView
import com.servalabs.chat.badges.BadgeImageView
import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.components.settings.PreferenceModel
import com.servalabs.chat.database.model.StoryViewState
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.adapter.mapping.MappingViewHolder

/**
 * Renders a large avatar (80dp) for a given Recipient.
 */
object AvatarPreference {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.conversation_settings_avatar_preference_item))
  }

  class Model(
    val recipient: Recipient,
    val storyViewState: StoryViewState,
    val onAvatarClick: (AvatarView) -> Unit,
    val onBadgeClick: (Badge) -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return recipient == newItem.recipient
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return super.areContentsTheSame(newItem) &&
        recipient.hasSameContent(newItem.recipient) &&
        storyViewState == newItem.storyViewState
    }
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {
    private val avatar: AvatarView = itemView.findViewById<AvatarView>(R.id.bio_preference_avatar)

    private val badge: BadgeImageView = itemView.findViewById(R.id.bio_preference_badge)

    init {
      ViewCompat.setTransitionName(avatar.parent as View, "avatar")
    }

    override fun bind(model: Model) {
      if (model.recipient.isSelf) {
        badge.setBadge(null)
        badge.setOnClickListener(null)
      } else {
        badge.setBadgeFromRecipient(model.recipient)
        badge.setOnClickListener {
          val badge = model.recipient.badges.firstOrNull()
          if (badge != null) {
            model.onBadgeClick(badge)
          }
        }
      }

      avatar.setStoryRingFromState(model.storyViewState)
      avatar.displayChatAvatar(model.recipient)
      avatar.disableQuickContact()
      avatar.setOnClickListener { model.onAvatarClick(avatar) }
    }
  }
}
