package com.servalabs.chat.stories.archive

import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.jobs.MultiDeviceDeleteSyncJob

class StoryArchiveRepository {

  fun deleteStories(messageIds: Set<Long>) {
    val records = messageIds.mapNotNull { SignalDatabase.messages.getMessageRecordOrNull(it) }.toSet()
    messageIds.forEach { SignalDatabase.messages.deleteMessage(it) }
    MultiDeviceDeleteSyncJob.enqueueMessageDeletes(records)
  }
}
