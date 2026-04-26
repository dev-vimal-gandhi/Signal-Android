/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.backup.v2.ui.status

sealed interface ArchiveUploadStatusBannerViewEvents {
  data object BannerClicked : ArchiveUploadStatusBannerViewEvents
  data object CancelClicked : ArchiveUploadStatusBannerViewEvents
  data object HideClicked : ArchiveUploadStatusBannerViewEvents
}
