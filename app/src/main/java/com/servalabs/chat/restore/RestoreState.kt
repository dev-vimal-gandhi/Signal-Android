/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.restore

import android.content.Intent
import android.net.Uri
import com.servalabs.chat.restore.transferorrestore.BackupRestorationType

/**
 * Shared state holder for the restore flow.
 */
data class RestoreState(val restorationType: BackupRestorationType = BackupRestorationType.LOCAL_BACKUP, val backupFile: Uri? = null, val nextIntent: Intent? = null)
