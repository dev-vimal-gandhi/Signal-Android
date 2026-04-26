package com.servalabs.chat.components.settings.models

import android.view.View
import android.widget.TextView
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.PreferenceModel
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.adapter.mapping.MappingViewHolder
import com.servalabs.chat.util.visible

object Progress {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.dsl_progress_pref))
  }

  data class Model(
    override val title: DSLSettingsText?
  ) : PreferenceModel<Model>()

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val title: TextView = itemView.findViewById(R.id.dsl_progress_pref_title)

    override fun bind(model: Model) {
      title.text = model.title?.resolve(context)
      title.visible = model.title != null
    }
  }
}
