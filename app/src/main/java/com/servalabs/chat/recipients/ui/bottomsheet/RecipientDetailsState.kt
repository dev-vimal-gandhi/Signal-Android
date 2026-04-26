package com.servalabs.chat.recipients.ui.bottomsheet

import com.servalabs.chat.groups.memberlabel.StyledMemberLabel

data class RecipientDetailsState(
  val memberLabel: StyledMemberLabel?,
  val aboutText: String?
)
