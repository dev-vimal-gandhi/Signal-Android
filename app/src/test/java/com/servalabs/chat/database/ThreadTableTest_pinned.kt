/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.database

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.signal.core.util.CursorUtil
import com.servalabs.chat.components.settings.app.chats.folders.ChatFolderRecord
import com.servalabs.chat.conversationlist.model.ConversationFilter
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.testutil.RecipientTestRule

@Suppress("ClassName")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class)
class ThreadTableTest_pinned {

  @get:Rule
  val recipients = RecipientTestRule()

  private lateinit var recipient: RecipientId
  private val allChats: ChatFolderRecord = ChatFolderRecord(folderType = ChatFolderRecord.FolderType.ALL)

  @Before
  fun setUp() {
    recipient = recipients.createRecipient("Alice Android")
  }

  @Test
  fun givenAPinnedThread_whenIDeleteTheLastMessage_thenIDoNotDeleteOrUnpinTheThread() {
    // GIVEN
    val threadId = SignalDatabase.threads.getOrCreateThreadIdFor(Recipient.resolved(recipient))
    val messageId = recipients.insertOutgoingMessage(recipient)
    SignalDatabase.threads.pinConversations(listOf(threadId))

    // WHEN
    SignalDatabase.messages.deleteMessage(messageId)

    // THEN
    val pinned = SignalDatabase.threads.getPinnedThreadIds()
    assertTrue(threadId in pinned)
  }

  @Test
  fun givenAPinnedThread_whenIDeleteTheLastMessage_thenIExpectTheThreadInUnarchivedCount() {
    // GIVEN
    val threadId = SignalDatabase.threads.getOrCreateThreadIdFor(Recipient.resolved(recipient))
    val messageId = recipients.insertOutgoingMessage(recipient)
    SignalDatabase.threads.pinConversations(listOf(threadId))

    // WHEN
    SignalDatabase.messages.deleteMessage(messageId)

    // THEN
    val unarchivedCount = SignalDatabase.threads.getUnarchivedConversationListCount(ConversationFilter.OFF, allChats)
    assertEquals(1, unarchivedCount)
  }

  @Test
  fun givenAPinnedThread_whenIDeleteTheLastMessage_thenIExpectPinnedThreadInUnarchivedList() {
    // GIVEN
    val threadId = SignalDatabase.threads.getOrCreateThreadIdFor(Recipient.resolved(recipient))
    val messageId = recipients.insertOutgoingMessage(recipient)
    SignalDatabase.threads.pinConversations(listOf(threadId))

    // WHEN
    SignalDatabase.messages.deleteMessage(messageId)

    // THEN
    SignalDatabase.threads.getUnarchivedConversationList(ConversationFilter.OFF, true, 0, 1, allChats).use {
      it.moveToFirst()
      assertEquals(threadId, CursorUtil.requireLong(it, ThreadTable.ID))
    }
  }
}
