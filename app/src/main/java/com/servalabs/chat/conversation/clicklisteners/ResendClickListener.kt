/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.conversation.clicklisteners

import android.view.View
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import com.servalabs.chat.database.model.MessageRecord
import com.servalabs.chat.mms.Slide
import com.servalabs.chat.mms.SlidesClickedListener
import com.servalabs.chat.sms.MessageSender

class ResendClickListener(private val messageRecord: MessageRecord) : SlidesClickedListener {
  override fun onClick(v: View?, slides: MutableList<Slide>?) {
    if (v == null) {
      Log.w(TAG, "Could not resend message, view was null!")
      return
    }

    SignalExecutors.BOUNDED.execute {
      MessageSender.resend(v.context, messageRecord)
    }
  }

  companion object {
    private val TAG = Log.tag(ResendClickListener::class.java)
  }
}
