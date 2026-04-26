package com.servalabs.chat.components.settings.models

import android.view.View
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.PreferenceModel
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.adapter.mapping.MappingViewHolder

object IndeterminateLoadingCircle : PreferenceModel<IndeterminateLoadingCircle>() {
  override fun areItemsTheSame(newItem: IndeterminateLoadingCircle): Boolean = true

  private class ViewHolder(itemView: View) : MappingViewHolder<IndeterminateLoadingCircle>(itemView) {
    override fun bind(model: IndeterminateLoadingCircle) = Unit
  }

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(IndeterminateLoadingCircle::class.java, LayoutFactory({ ViewHolder(it) }, R.layout.indeterminate_loading_circle_pref))
  }
}
