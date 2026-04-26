package com.servalabs.chat.safety

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.servalabs.chat.contacts.paged.ContactSearchKey
import com.servalabs.chat.database.model.MessageId
import com.servalabs.chat.recipients.RecipientId

/**
 * Fragment argument for `SafetyNumberBottomSheetFragment`
 */
@Parcelize
data class SafetyNumberBottomSheetArgs(
  val untrustedRecipients: List<RecipientId>,
  val destinations: List<ContactSearchKey.RecipientSearchKey>,
  val messageId: MessageId? = null
) : Parcelable
