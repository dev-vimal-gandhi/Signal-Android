/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.database.model

import com.servalabs.chat.attachments.DatabaseAttachment
import com.servalabs.chat.database.CallTable
import com.servalabs.chat.payments.Payment
import com.servalabs.chat.polls.PollRecord

fun MessageRecord.withReactions(reactions: List<ReactionRecord>): MessageRecord {
  return if (this is MmsMessageRecord) {
    this.withReactions(reactions)
  } else {
    this
  }
}

fun MessageRecord.withAttachments(attachments: List<DatabaseAttachment>): MessageRecord {
  return if (this is MmsMessageRecord) {
    this.withAttachments(attachments)
  } else {
    this
  }
}
fun MessageRecord.withPayment(payment: Payment): MessageRecord {
  return if (this is MmsMessageRecord) {
    this.withPayment(payment)
  } else {
    this
  }
}

fun MessageRecord.withCall(call: CallTable.Call): MessageRecord {
  return if (this is MmsMessageRecord) {
    this.withCall(call)
  } else {
    this
  }
}

fun MessageRecord.withPoll(poll: PollRecord): MessageRecord {
  return if (this is MmsMessageRecord) {
    this.withPoll(poll)
  } else {
    this
  }
}
