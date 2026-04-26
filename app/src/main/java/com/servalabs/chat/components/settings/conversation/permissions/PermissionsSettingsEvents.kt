package com.servalabs.chat.components.settings.conversation.permissions

import com.servalabs.chat.groups.ui.GroupChangeFailureReason

sealed class PermissionsSettingsEvents {
  class GroupChangeError(val reason: GroupChangeFailureReason) : PermissionsSettingsEvents()
  object ShowMemberLabelsWillBeRemovedWarning : PermissionsSettingsEvents()
}
