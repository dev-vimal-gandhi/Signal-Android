package com.servalabs.chat.conversation

import android.os.Bundle
import androidx.core.view.WindowCompat
import com.servalabs.chat.R
import com.servalabs.chat.conversation.v2.ConversationActivity
import com.servalabs.chat.util.ViewUtil

/**
 * Activity which encapsulates a conversation for a Bubble window.
 *8
 * This activity exists so that we can override some of its manifest parameters
 * without clashing with [ConversationActivity] and provide an API-level
 * independent "is in bubble?" check.
 */
class BubbleConversationActivity : ConversationActivity() {

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState, ready)
  }

  override fun onPause() {
    super.onPause()
    ViewUtil.hideKeyboard(this, findViewById(R.id.fragment_container))
  }
}
