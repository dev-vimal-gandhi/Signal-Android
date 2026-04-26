package com.servalabs.chat.stories.settings.story

import com.servalabs.chat.contacts.paged.ContactSearchData
import com.servalabs.chat.stories.archive.StoryArchiveDuration

data class StoriesPrivacySettingsState(
  val areStoriesEnabled: Boolean,
  val areViewReceiptsEnabled: Boolean,
  val isUpdatingEnabledState: Boolean = false,
  val storyContactItems: List<ContactSearchData> = emptyList(),
  val userHasStories: Boolean = false,
  val isArchiveEnabled: Boolean = false,
  val archiveDuration: StoryArchiveDuration = StoryArchiveDuration.THIRTY_DAYS
)
