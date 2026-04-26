/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.backup

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.servalabs.chat.keyvalue.protos.LocalBackupCreationProgress

object LocalExportProgress {
  val internalEncryptedProgress = MutableStateFlow(LocalBackupCreationProgress())
  val internalPlaintextProgress = MutableStateFlow(LocalBackupCreationProgress())

  val encryptedProgress: StateFlow<LocalBackupCreationProgress> = internalEncryptedProgress
  val plaintextProgress: StateFlow<LocalBackupCreationProgress> = internalPlaintextProgress

  fun setEncryptedProgress(progress: LocalBackupCreationProgress) {
    internalEncryptedProgress.value = progress
  }

  fun setPlaintextProgress(progress: LocalBackupCreationProgress) {
    internalPlaintextProgress.value = progress
  }
}
