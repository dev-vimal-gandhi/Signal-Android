/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.banner.banners

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import com.servalabs.chat.backup.v2.ArchiveRestoreProgress
import com.servalabs.chat.backup.v2.ArchiveRestoreProgressState
import com.servalabs.chat.backup.v2.ArchiveRestoreProgressState.RestoreStatus
import com.servalabs.chat.backup.v2.ui.status.ArchiveRestoreStatusBanner
import com.servalabs.chat.banner.Banner

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveRestoreStatusBanner(private val listener: RestoreProgressBannerListener) : Banner<ArchiveRestoreProgressState>() {

  override val enabled: Boolean
    get() = ArchiveRestoreProgress.state.let { it.restoreState.isMediaRestoreOperation || it.restoreStatus == RestoreStatus.FINISHED }

  override val dataFlow: Flow<ArchiveRestoreProgressState> by lazy {
    ArchiveRestoreProgress
      .stateFlow
      .filter {
        it.restoreStatus != RestoreStatus.NONE && (it.restoreState.isMediaRestoreOperation || it.restoreStatus == RestoreStatus.FINISHED)
      }
  }

  @Composable
  override fun DisplayBanner(model: ArchiveRestoreProgressState, contentPadding: PaddingValues) {
    ArchiveRestoreStatusBanner(
      data = model,
      onBannerClick = listener::onBannerClick,
      onActionClick = listener::onActionClick,
      onDismissClick = {
        ArchiveRestoreProgress.clearFinishedStatus()
        listener.onDismissComplete()
      }
    )
  }

  interface RestoreProgressBannerListener {
    fun onBannerClick()
    fun onActionClick(data: ArchiveRestoreProgressState)
    fun onDismissComplete()
  }
}
