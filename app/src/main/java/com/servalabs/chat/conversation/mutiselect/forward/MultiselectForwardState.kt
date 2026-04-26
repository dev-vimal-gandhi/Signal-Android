package com.servalabs.chat.conversation.mutiselect.forward

import com.servalabs.chat.contacts.paged.ContactSearchKey
import com.servalabs.chat.database.model.IdentityRecord
import com.servalabs.chat.stories.Stories

data class MultiselectForwardState(
  val stage: Stage = Stage.Selection,
  val storySendRequirements: Stories.MediaTransform.SendRequirements = Stories.MediaTransform.SendRequirements.CAN_NOT_SEND
) {

  sealed class Stage {
    object Selection : Stage()
    object FirstConfirmation : Stage()
    object LoadingIdentities : Stage()
    data class SafetyConfirmation(val identities: List<IdentityRecord>, val selectedContacts: List<ContactSearchKey>) : Stage()
    object SendPending : Stage()
    object SomeFailed : Stage()
    object AllFailed : Stage()
    object Success : Stage()
    data class SelectionConfirmed(val selectedContacts: Set<ContactSearchKey>) : Stage()
  }
}
