package com.servalabs.chat.search

import com.servalabs.chat.database.model.ThreadRecord

data class ThreadSearchResult(val results: List<ThreadRecord>, val query: String)
