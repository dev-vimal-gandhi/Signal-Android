/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.backups.local

import org.signal.core.models.AccountEntropyPool
import com.servalabs.chat.components.settings.app.backups.remote.BackupKeySaveState
import com.servalabs.chat.keyvalue.SignalStore

data class LocalBackupsKeyState(
  val accountEntropyPool: AccountEntropyPool = SignalStore.account.accountEntropyPool,
  val keySaveState: BackupKeySaveState? = null
)
