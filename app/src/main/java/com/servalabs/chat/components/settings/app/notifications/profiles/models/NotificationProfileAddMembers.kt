package com.servalabs.chat.components.settings.app.notifications.profiles.models

import android.view.View
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.DSLSettingsIcon
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.NO_TINT
import com.servalabs.chat.components.settings.PreferenceModel
import com.servalabs.chat.components.settings.PreferenceViewHolder
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter

/**
 * Custom DSL preference for adding members to a profile.
 */
object NotificationProfileAddMembers {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.large_icon_preference_item))
  }

  class Model(
    override val title: DSLSettingsText = DSLSettingsText.from(R.string.AddAllowedMembers__add_people_or_groups),
    override val icon: DSLSettingsIcon = DSLSettingsIcon.from(R.drawable.add_to_a_group, NO_TINT),
    val onClick: (Long, Set<RecipientId>) -> Unit,
    val profileId: Long,
    val currentSelection: Set<RecipientId>
  ) : PreferenceModel<Model>() {
    override fun areContentsTheSame(newItem: Model): Boolean {
      return super.areContentsTheSame(newItem) && profileId == newItem.profileId && currentSelection == newItem.currentSelection
    }
  }

  private class ViewHolder(itemView: View) : PreferenceViewHolder<Model>(itemView) {
    override fun bind(model: Model) {
      super.bind(model)
      itemView.setOnClickListener { model.onClick(model.profileId, model.currentSelection) }
    }
  }
}
