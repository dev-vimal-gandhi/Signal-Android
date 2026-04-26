/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.backup.v2.database

import org.signal.core.util.select
import org.signal.core.util.withinTransaction
import com.servalabs.chat.backup.v2.ExportState
import com.servalabs.chat.backup.v2.exporters.DistributionListArchiveExporter
import com.servalabs.chat.database.DistributionListTables
import com.servalabs.chat.database.model.DistributionListId
import com.servalabs.chat.database.model.DistributionListPrivacyMode
import com.servalabs.chat.recipients.RecipientId

fun DistributionListTables.getAllForBackup(selfRecipientId: RecipientId, exportState: ExportState): DistributionListArchiveExporter {
  val cursor = readableDatabase
    .select()
    .from(DistributionListTables.ListTable.TABLE_NAME)
    .run()

  return DistributionListArchiveExporter(cursor, this, selfRecipientId, exportState)
}

fun DistributionListTables.getMembersForBackup(id: DistributionListId): List<RecipientId> {
  lateinit var privacyMode: DistributionListPrivacyMode
  lateinit var rawMembers: List<RecipientId>

  readableDatabase.withinTransaction {
    privacyMode = getPrivacyMode(id)
    rawMembers = getRawMembers(id, privacyMode)
  }

  return when (privacyMode) {
    DistributionListPrivacyMode.ALL -> emptyList()
    DistributionListPrivacyMode.ONLY_WITH -> rawMembers
    DistributionListPrivacyMode.ALL_EXCEPT -> rawMembers
  }
}
