package com.servalabs.chat.stories.settings.create

import androidx.navigation.fragment.findNavController
import com.servalabs.chat.R
import com.servalabs.chat.database.model.DistributionListId
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.stories.settings.select.BaseStoryRecipientSelectionFragment
import com.servalabs.chat.util.navigation.safeNavigate

/**
 * Allows user to select who will see the story they are creating
 */
class CreateStoryViewerSelectionFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.CreateStoryViewerSelectionFragment__next
  override val distributionListId: DistributionListId? = null

  override fun goToNextScreen(recipients: Set<RecipientId>) {
    findNavController().safeNavigate(CreateStoryViewerSelectionFragmentDirections.actionCreateStoryViewerSelectionToCreateStoryWithViewers(recipients.toTypedArray()))
  }
}
