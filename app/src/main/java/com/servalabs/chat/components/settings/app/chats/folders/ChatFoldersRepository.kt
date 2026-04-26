package com.servalabs.chat.components.settings.app.chats.folders

import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.storage.StorageSyncHelper

/**
 * Repository for chat folders that handles creation, deletion, listing, etc.,
 */
object ChatFoldersRepository {

  fun getCurrentFolders(): List<ChatFolderRecord> {
    return SignalDatabase.chatFolders.getCurrentChatFolders()
  }

  fun getUnreadCountAndEmptyAndMutedStatusForFolders(folders: List<ChatFolderRecord>): HashMap<Long, Triple<Int, Boolean, Boolean>> {
    return SignalDatabase.chatFolders.getUnreadCountAndEmptyAndMutedStatusForFolders(folders)
  }

  fun createFolder(folder: ChatFolderRecord, includedRecipients: Set<Recipient>, excludedRecipients: Set<Recipient>) {
    val includedChats = includedRecipients.map { recipient -> SignalDatabase.threads.getOrCreateThreadIdFor(recipient) }
    val excludedChats = excludedRecipients.map { recipient -> SignalDatabase.threads.getOrCreateThreadIdFor(recipient) }
    val updatedFolder = folder.copy(
      includedChats = includedChats,
      excludedChats = excludedChats
    )

    SignalDatabase.chatFolders.createFolder(updatedFolder)
    StorageSyncHelper.scheduleSyncForDataChange()
  }

  fun updateFolder(folder: ChatFolderRecord, includedRecipients: Set<Recipient>, excludedRecipients: Set<Recipient>) {
    val includedChats = includedRecipients.map { recipient -> SignalDatabase.threads.getOrCreateThreadIdFor(recipient) }
    val excludedChats = excludedRecipients.map { recipient -> SignalDatabase.threads.getOrCreateThreadIdFor(recipient) }
    val updatedFolder = folder.copy(
      includedChats = includedChats,
      excludedChats = excludedChats
    )

    SignalDatabase.chatFolders.updateFolder(updatedFolder)
    scheduleSync(updatedFolder.id)
  }

  fun deleteFolder(folder: ChatFolderRecord) {
    SignalDatabase.chatFolders.deleteChatFolder(folder)
    scheduleSync(folder.id)
  }

  fun updatePositions(folders: List<ChatFolderRecord>) {
    SignalDatabase.chatFolders.updatePositions(folders)
    folders.forEach { scheduleSync(it.id) }
  }

  fun getFolder(id: Long): ChatFolderRecord {
    return SignalDatabase.chatFolders.getChatFolder(id)!!
  }

  fun getFolderCount(): Int {
    return SignalDatabase.chatFolders.getFolderCount()
  }

  private fun scheduleSync(id: Long) {
    SignalDatabase.chatFolders.markNeedsSync(id)
    StorageSyncHelper.scheduleSyncForDataChange()
  }
}
