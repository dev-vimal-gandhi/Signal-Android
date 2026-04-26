package com.servalabs.chat.components.settings.app.subscription.models

import com.servalabs.chat.databinding.PaypalButtonBinding
import com.servalabs.chat.util.adapter.mapping.BindingFactory
import com.servalabs.chat.util.adapter.mapping.BindingViewHolder
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.adapter.mapping.MappingModel

object PayPalButton {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, BindingFactory(::ViewHolder, PaypalButtonBinding::inflate))
  }

  class Model(val onClick: () -> Unit, val isEnabled: Boolean) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean = true
    override fun areContentsTheSame(newItem: Model): Boolean = isEnabled == newItem.isEnabled
  }

  class ViewHolder(binding: PaypalButtonBinding) : BindingViewHolder<Model, PaypalButtonBinding>(binding) {
    override fun bind(model: Model) {
      binding.paypalButton.isEnabled = model.isEnabled
      binding.paypalButton.setOnClickListener {
        binding.paypalButton.isEnabled = false
        model.onClick()
      }
    }
  }
}
