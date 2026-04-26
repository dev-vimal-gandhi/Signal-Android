package com.servalabs.chat.safety

import com.servalabs.chat.contacts.paged.ContactSearchKey
import com.servalabs.chat.conversation.ui.error.TrustAndVerifyResult

/** One-shot side effects emitted by [SafetyNumberBottomSheetViewModel] for the fragment to handle. */
sealed interface SafetyNumberBottomSheetEffect {
  /**
   * The trust-and-verify operation finished. The fragment should inspect [result],
   * fire the appropriate [SafetyNumberBottomSheet.Callbacks] method, then dismiss.
   */
  data class TrustCompleted(
    val result: TrustAndVerifyResult,
    val destinations: List<ContactSearchKey.RecipientSearchKey>
  ) : SafetyNumberBottomSheetEffect
}
