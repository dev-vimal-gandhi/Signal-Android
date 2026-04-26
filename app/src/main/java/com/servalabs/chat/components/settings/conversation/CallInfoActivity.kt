package com.servalabs.chat.components.settings.conversation

import com.servalabs.chat.util.DynamicNoActionBarTheme
import com.servalabs.chat.util.DynamicTheme

class CallInfoActivity : ConversationSettingsActivity(), ConversationSettingsFragment.TransitionCallback {

  override val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
}
