package com.servalabs.chat.components.settings.app.notifications.profiles

import com.servalabs.chat.notifications.profiles.NotificationProfile
import com.servalabs.chat.notifications.profiles.NotificationProfiles

data class NotificationProfilesState(
  val profiles: List<NotificationProfile>,
  val activeProfile: NotificationProfile? = NotificationProfiles.getActiveProfile(profiles)
)
