/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.banner.banners

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.flow.flowOf
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import com.servalabs.chat.R
import com.servalabs.chat.banner.Banner
import com.servalabs.chat.banner.ui.compose.Action
import com.servalabs.chat.banner.ui.compose.DefaultBanner
import com.servalabs.chat.banner.ui.compose.Importance
import com.servalabs.chat.contacts.sync.CdsPermanentErrorBottomSheet
import com.servalabs.chat.keyvalue.SignalStore
import kotlin.time.Duration.Companion.days

class CdsPermanentErrorBanner(private val fragmentManager: FragmentManager) : Banner<Unit>() {

  companion object {
    /**
     * Even if we're not truly "permanently blocked", if the time until we're unblocked is long enough, we'd rather show the permanent error message than
     * telling the user to wait for 3 months or something.
     */
    val PERMANENT_TIME_CUTOFF = 30.days.inWholeMilliseconds
  }

  override val enabled: Boolean
    get() {
      val timeUntilUnblock = SignalStore.misc.cdsBlockedUtil - System.currentTimeMillis()
      return SignalStore.misc.isCdsBlocked && timeUntilUnblock >= PERMANENT_TIME_CUTOFF
    }

  override val dataFlow
    get() = flowOf(Unit)

  @Composable
  override fun DisplayBanner(model: Unit, contentPadding: PaddingValues) {
    Banner(
      contentPadding = contentPadding,
      onLearnMoreClicked = { CdsPermanentErrorBottomSheet.show(fragmentManager) }
    )
  }
}

@Composable
private fun Banner(contentPadding: PaddingValues, onLearnMoreClicked: () -> Unit = {}) {
  DefaultBanner(
    title = null,
    body = stringResource(id = R.string.reminder_cds_permanent_error_body),
    importance = Importance.ERROR,
    actions = listOf(
      Action(R.string.reminder_cds_permanent_error_learn_more) {
        onLearnMoreClicked()
      }
    ),
    paddingValues = contentPadding
  )
}

@DayNightPreviews
@Composable
private fun BannerPreview() {
  Previews.Preview {
    Banner(PaddingValues(0.dp))
  }
}
