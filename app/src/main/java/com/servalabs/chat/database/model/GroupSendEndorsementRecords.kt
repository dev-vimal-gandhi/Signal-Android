/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.database.model

import org.signal.libsignal.zkgroup.groupsend.GroupSendEndorsement
import com.servalabs.chat.recipients.RecipientId

/**
 * Contains the individual group send endorsements for a specific group
 * source from our local db.
 */
data class GroupSendEndorsementRecords(val endorsements: Map<RecipientId, GroupSendEndorsement?>) {
  fun getEndorsement(recipientId: RecipientId): GroupSendEndorsement? {
    return endorsements[recipientId]
  }

  fun isMissingAnyEndorsements(): Boolean {
    return endorsements.values.any { it == null }
  }
}
