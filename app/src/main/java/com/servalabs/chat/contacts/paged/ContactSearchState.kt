package com.servalabs.chat.contacts.paged

import com.servalabs.chat.conversationlist.chatfilter.ConversationFilterRequest
import com.servalabs.chat.search.SearchFilter

/**
 * Simple search state for contacts.
 */
data class ContactSearchState(
  val query: String? = null,
  val conversationFilterRequest: ConversationFilterRequest? = null,
  val expandedSections: Set<ContactSearchConfiguration.SectionKey> = emptySet(),
  val groupStories: Set<ContactSearchData.Story> = emptySet(),
  val searchFilter: SearchFilter = SearchFilter.EMPTY
)
