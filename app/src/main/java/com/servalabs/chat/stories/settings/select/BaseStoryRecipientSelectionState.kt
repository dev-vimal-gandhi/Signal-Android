package com.servalabs.chat.stories.settings.select

import com.servalabs.chat.database.model.DistributionListId
import com.servalabs.chat.database.model.DistributionListRecord
import com.servalabs.chat.recipients.RecipientId

data class BaseStoryRecipientSelectionState(
  val distributionListId: DistributionListId?,
  val privateStory: DistributionListRecord? = null,
  val selection: Set<RecipientId> = emptySet(),
  val isStartingSelection: Boolean = false
)
