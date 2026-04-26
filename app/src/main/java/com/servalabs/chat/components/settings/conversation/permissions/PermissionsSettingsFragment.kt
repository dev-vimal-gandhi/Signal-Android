package com.servalabs.chat.components.settings.conversation.permissions

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.DSLConfiguration
import com.servalabs.chat.components.settings.DSLSettingsFragment
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.configure
import com.servalabs.chat.groups.GroupId
import com.servalabs.chat.groups.ui.GroupErrors
import com.servalabs.chat.util.adapter.mapping.MappingAdapter

class PermissionsSettingsFragment : DSLSettingsFragment(
  titleId = R.string.ConversationSettingsFragment__permissions
) {

  private val permissionsOptions: Array<String> by lazy {
    resources.getStringArray(R.array.PermissionsSettingsFragment__editor_labels)
  }

  private val viewModel: PermissionsSettingsViewModel by viewModels(
    factoryProducer = {
      val args = PermissionsSettingsFragmentArgs.fromBundle(requireArguments())
      val groupId = requireNotNull(args.groupId as GroupId)
      val repository = PermissionsSettingsRepository(requireContext())

      PermissionsSettingsViewModel.Factory(groupId, repository)
    }
  )

  override fun bindAdapter(adapter: MappingAdapter) {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }

    viewModel.events.observe(viewLifecycleOwner) { event ->
      when (event) {
        is PermissionsSettingsEvents.GroupChangeError -> handleGroupChangeError(event)
        is PermissionsSettingsEvents.ShowMemberLabelsWillBeRemovedWarning -> showMemberLabelsWillBeRemovedDialog()
      }
    }
  }

  private fun handleGroupChangeError(groupChangeError: PermissionsSettingsEvents.GroupChangeError) {
    Toast.makeText(context, GroupErrors.getUserDisplayMessage(groupChangeError.reason), Toast.LENGTH_LONG).show()
  }

  private fun getConfiguration(state: PermissionsSettingsState): DSLConfiguration {
    return configure {
      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__add_members),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_add_new_members),
        selected = getSelected(state.nonAdminCanAddMembers),
        confirmAction = true,
        onSelected = {
          viewModel.setNonAdminCanAddMembers(it == 1)
        }
      )

      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__edit_group_info),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_edit_this_groups_info),
        selected = getSelected(state.nonAdminCanEditGroupInfo),
        confirmAction = true,
        onSelected = {
          viewModel.setNonAdminCanEditGroupInfo(it == 1)
        }
      )

      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__send_messages),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_send_messages),
        selected = getSelected(!state.announcementGroup),
        confirmAction = true,
        onSelected = {
          viewModel.setAnnouncementGroup(it == 0)
        }
      )

      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__add_member_labels),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_add_member_labels),
        selected = getSelected(state.nonAdminCanSetMemberLabel),
        confirmAction = true,
        onSelected = { selectedIndex ->
          if (selectedIndex >= 0) {
            viewModel.onMemberLabelPermissionChangeRequested(nonAdminCanSetMemberLabel = selectedIndex == 1)
          }
        }
      )
    }
  }

  private fun showMemberLabelsWillBeRemovedDialog() {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.PermissionsSettingsFragment__member_labels_will_be_cleared_title)
      .setMessage(R.string.PermissionsSettingsFragment__member_labels_will_be_cleared_body)
      .setPositiveButton(R.string.PermissionsSettingsFragment__change_permission) { _, _ ->
        viewModel.onRestrictMemberLabelsToAdminsConfirmed()
      }
      .setNegativeButton(android.R.string.cancel, null)
      .show()
  }

  @StringRes
  private fun getSelected(isNonAdminAllowed: Boolean): Int {
    return if (isNonAdminAllowed) {
      1
    } else {
      0
    }
  }
}
