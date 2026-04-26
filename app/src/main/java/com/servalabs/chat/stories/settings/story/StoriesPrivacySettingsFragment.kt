package com.servalabs.chat.stories.settings.story

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.signal.core.ui.BottomSheetUtil
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.dp
import com.servalabs.chat.R
import com.servalabs.chat.components.DialogFragmentDisplayManager
import com.servalabs.chat.components.ProgressCardDialogFragment
import com.servalabs.chat.components.settings.DSLConfiguration
import com.servalabs.chat.components.settings.DSLSettingsAdapter
import com.servalabs.chat.components.settings.DSLSettingsFragment
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.configure
import com.servalabs.chat.contacts.paged.ContactSearchAdapter
import com.servalabs.chat.contacts.paged.ContactSearchKey
import com.servalabs.chat.contacts.paged.ContactSearchPagedDataSourceRepository
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.mediasend.v2.stories.ChooseGroupStoryBottomSheet
import com.servalabs.chat.mediasend.v2.stories.ChooseStoryTypeBottomSheet
import com.servalabs.chat.stories.GroupStoryEducationSheet
import com.servalabs.chat.stories.archive.StoryArchiveDuration
import com.servalabs.chat.stories.dialogs.StoryDialogs
import com.servalabs.chat.stories.settings.create.CreateStoryFlowDialogFragment
import com.servalabs.chat.stories.settings.create.CreateStoryWithViewersFragment
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.adapter.mapping.PagingMappingAdapter
import com.servalabs.chat.util.navigation.safeNavigate
import org.signal.core.ui.R as CoreUiR

/**
 * Allows the user to view their stories they can send to and modify settings.
 */
class StoriesPrivacySettingsFragment :
  DSLSettingsFragment(
    titleId = R.string.preferences__stories
  ),
  ChooseStoryTypeBottomSheet.Callback,
  GroupStoryEducationSheet.Callback {

  private val viewModel: StoriesPrivacySettingsViewModel by viewModels(factoryProducer = {
    StoriesPrivacySettingsViewModel.Factory(ContactSearchPagedDataSourceRepository(requireContext()))
  })

  private val lifecycleDisposable = LifecycleDisposable()
  private val progressDisplayManager = DialogFragmentDisplayManager { ProgressCardDialogFragment.create() }

  override fun createAdapters(): Array<MappingAdapter> {
    return arrayOf(DSLSettingsAdapter(), PagingMappingAdapter<ContactSearchKey>(), DSLSettingsAdapter())
  }

  override fun bindAdapters(adapter: ConcatAdapter) {
    lifecycleDisposable.bindTo(viewLifecycleOwner)

    val titleId = StoriesPrivacySettingsFragmentArgs.fromBundle(requireArguments()).titleId
    setTitle(titleId)

    val (top, middle, bottom) = adapter.adapters

    findNavController().addOnDestinationChangedListener { _, destination, _ ->
      if (destination.id == R.id.storiesPrivacySettingsFragment) {
        viewModel.pagingController.onDataInvalidated()
      }
    }

    @Suppress("UNCHECKED_CAST")
    ContactSearchAdapter.registerStoryItems(
      mappingAdapter = middle as PagingMappingAdapter<ContactSearchKey>,
      storyListener = { _, story, _ ->
        when {
          story.recipient.isMyStory -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToMyStorySettings())
          story.recipient.isGroup -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToGroupStorySettings(story.recipient.requireGroupId()))
          else -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToPrivateStorySettings(story.recipient.requireDistributionListId()))
        }
      }
    )

    NewStoryItem.register(top as MappingAdapter)

    middle.setPagingController(viewModel.pagingController)

    parentFragmentManager.setFragmentResultListener(ChooseGroupStoryBottomSheet.GROUP_STORY, viewLifecycleOwner) { _, bundle ->
      val results = ChooseGroupStoryBottomSheet.ResultContract.getRecipientIds(bundle)
      viewModel.displayGroupsAsStories(results)
    }

    parentFragmentManager.setFragmentResultListener(CreateStoryWithViewersFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
      viewModel.pagingController.onDataInvalidated()
    }

    lifecycleDisposable += viewModel.state.subscribe { state ->
      if (state.isUpdatingEnabledState) {
        progressDisplayManager.show(viewLifecycleOwner, childFragmentManager)
      } else {
        progressDisplayManager.hide()
      }

      top.submitList(getTopConfiguration(state).toMappingModelList())
      middle.submitList(getMiddleConfiguration(state).toMappingModelList())
      (bottom as MappingAdapter).submitList(getBottomConfiguration(state).toMappingModelList())
    }
  }

  private fun getTopConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return configure {
      if (state.areStoriesEnabled) {
        space(16.dp)

        noPadTextPref(
          title = DSLSettingsText.from(
            R.string.StoriesPrivacySettingsFragment__story_updates_automatically_disappear,
            DSLSettingsText.TextAppearanceModifier(CoreUiR.style.Signal_Text_BodyMedium),
            DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), CoreUiR.color.signal_colorOnSurfaceVariant))
          )
        )

        space(20.dp)

        sectionHeaderPref(R.string.StoriesPrivacySettingsFragment__stories)

        customPref(
          NewStoryItem.Model {
            ChooseStoryTypeBottomSheet().show(childFragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
          }
        )
      } else {
        clickPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__turn_on_stories),
          summary = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__share_and_view),
          onClick = {
            viewModel.setStoriesEnabled(true)
          }
        )
      }
    }
  }

  private fun getMiddleConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return if (state.areStoriesEnabled) {
      configure {
        ContactSearchAdapter.toMappingModelList(
          state.storyContactItems,
          emptySet(),
          null
        ).forEach {
          customPref(it)
        }
      }
    } else {
      configure { }
    }
  }

  private fun getBottomConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return if (state.areStoriesEnabled) {
      configure {
        dividerPref()

        switchPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__view_receipts),
          summary = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__see_and_share),
          isChecked = state.areViewReceiptsEnabled,
          onClick = {
            viewModel.toggleViewReceipts()
          }
        )

        if (SignalStore.labs.storyArchive) {
          dividerPref()

          sectionHeaderPref(R.string.StoryArchive__archive)

          switchPref(
            title = DSLSettingsText.from(R.string.StoryArchive__keep_stories_in_archive),
            summary = DSLSettingsText.from(R.string.StoryArchive__save_stories_after_they_expire),
            isChecked = state.isArchiveEnabled,
            onClick = {
              viewModel.toggleArchiveEnabled()
            }
          )

          if (state.isArchiveEnabled) {
            clickPref(
              title = DSLSettingsText.from(R.string.StoryArchive__keep_stories_for),
              summary = DSLSettingsText.from(state.archiveDuration.labelRes),
              onClick = {
                val durations = StoryArchiveDuration.entries.toTypedArray()
                val labels = durations.map { getString(it.labelRes) }.toTypedArray()
                val checkedIndex = durations.indexOf(state.archiveDuration)

                MaterialAlertDialogBuilder(requireContext())
                  .setTitle(R.string.StoryArchive__keep_stories_for)
                  .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                    viewModel.setArchiveDuration(durations[which])
                    dialog.dismiss()
                  }
                  .setNegativeButton(android.R.string.cancel, null)
                  .show()
              }
            )
          }
        }

        dividerPref()

        clickPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__turn_off_stories),
          summary = DSLSettingsText.from(
            R.string.StoriesPrivacySettingsFragment__if_you_opt_out,
            DSLSettingsText.TextAppearanceModifier(CoreUiR.style.Signal_Text_BodyMedium),
            DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), CoreUiR.color.signal_colorOnSurfaceVariant))
          ),
          onClick = {
            StoryDialogs.disableStories(requireContext(), viewModel.userHasActiveStories) {
              viewModel.setStoriesEnabled(false)
            }
          }
        )
      }
    } else {
      configure { }
    }
  }

  override fun onGroupStoryClicked() {
    if (SignalStore.story.userHasSeenGroupStoryEducationSheet) {
      onGroupStoryEducationSheetNext()
    } else {
      GroupStoryEducationSheet().show(childFragmentManager, GroupStoryEducationSheet.KEY)
    }
  }

  override fun onNewStoryClicked() {
    CreateStoryFlowDialogFragment().show(parentFragmentManager, CreateStoryWithViewersFragment.REQUEST_KEY)
  }

  override fun onGroupStoryEducationSheetNext() {
    ChooseGroupStoryBottomSheet().show(parentFragmentManager, ChooseGroupStoryBottomSheet.GROUP_STORY)
  }
}
