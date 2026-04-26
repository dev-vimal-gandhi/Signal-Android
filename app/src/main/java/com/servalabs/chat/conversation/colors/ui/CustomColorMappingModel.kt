package com.servalabs.chat.conversation.colors.ui

import com.servalabs.chat.util.adapter.mapping.MappingModel

class CustomColorMappingModel : MappingModel<CustomColorMappingModel> {
  override fun areItemsTheSame(newItem: CustomColorMappingModel): Boolean {
    return true
  }

  override fun areContentsTheSame(newItem: CustomColorMappingModel): Boolean {
    return true
  }
}
