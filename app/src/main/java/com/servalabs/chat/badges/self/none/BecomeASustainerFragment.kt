package com.servalabs.chat.badges.self.none

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import org.signal.core.ui.BottomSheetUtil
import org.signal.core.util.DimensionUnit
import com.servalabs.chat.R
import com.servalabs.chat.badges.models.BadgePreview
import com.servalabs.chat.components.settings.DSLConfiguration
import com.servalabs.chat.components.settings.DSLSettingsAdapter
import com.servalabs.chat.components.settings.DSLSettingsBottomSheetFragment
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.app.AppSettingsActivity
import com.servalabs.chat.components.settings.configure
import org.signal.core.ui.R as CoreUiR

class BecomeASustainerFragment : DSLSettingsBottomSheetFragment() {

  private val viewModel: BecomeASustainerViewModel by viewModels()

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    BadgePreview.register(adapter)

    viewModel.state.observe(viewLifecycleOwner) {
      adapter.submitList(getConfiguration(it).toMappingModelList())
    }
  }

  private fun getConfiguration(state: BecomeASustainerState): DSLConfiguration {
    return configure {
      customPref(BadgePreview.BadgeModel.FeaturedModel(badge = state.badge))

      sectionHeaderPref(
        title = DSLSettingsText.from(
          R.string.BecomeASustainerFragment__get_badges,
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TitleLargeModifier
        )
      )

      space(DimensionUnit.DP.toPixels(8f).toInt())

      noPadTextPref(
        title = DSLSettingsText.from(
          R.string.BecomeASustainerFragment__signal_is_a_non_profit,
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TextAppearanceModifier(CoreUiR.style.Signal_Text_BodyMedium),
          DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), CoreUiR.color.signal_colorOnSurfaceVariant))
        )
      )

      space(DimensionUnit.DP.toPixels(32f).toInt())

      tonalWrappedButton(
        text = DSLSettingsText.from(
          R.string.BecomeASustainerMegaphone__become_a_sustainer
        ),
        onClick = {
          requireActivity().finish()
          requireActivity().startActivity(AppSettingsActivity.subscriptions(requireContext()).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
      )

      space(DimensionUnit.DP.toPixels(32f).toInt())
    }
  }

  companion object {
    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      BecomeASustainerFragment().show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }
  }
}
