package com.servalabs.chat.mediasend.v2.gallery

import com.servalabs.chat.util.adapter.mapping.MappingModel

data class MediaGalleryState(
  val bucketId: String?,
  val bucketTitle: String?,
  val items: List<MappingModel<*>> = listOf()
)
