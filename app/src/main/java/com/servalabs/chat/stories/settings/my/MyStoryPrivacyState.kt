package com.servalabs.chat.stories.settings.my

import com.servalabs.chat.database.model.DistributionListPrivacyMode

data class MyStoryPrivacyState(val privacyMode: DistributionListPrivacyMode? = null, val connectionCount: Int = 0)
