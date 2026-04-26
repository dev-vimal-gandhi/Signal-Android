/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.conversation.permissions

import androidx.lifecycle.MutableLiveData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.servalabs.chat.groups.GroupAccessControl
import com.servalabs.chat.groups.GroupId
import com.servalabs.chat.groups.LiveGroup
import com.servalabs.chat.util.livedata.LiveDataRule
import com.servalabs.chat.util.livedata.LiveDataTestUtil

class PermissionsSettingsViewModelTest {
  @get:Rule
  val liveDataRule = LiveDataRule()

  private val groupId = mockk<GroupId.V2>()
  private val repository = mockk<PermissionsSettingsRepository>(relaxUnitFun = true)

  private fun createViewModel(
    memberLabelAccessControl: GroupAccessControl = GroupAccessControl.ONLY_ADMINS,
    nonAdminMembersHaveLabels: Boolean = true
  ): PermissionsSettingsViewModel {
    val liveGroup = mockk<LiveGroup> {
      every { isSelfAdmin } returns MutableLiveData(false)
      every { isActive } returns MutableLiveData(true)
      every { membershipAdditionAccessControl } returns MutableLiveData(GroupAccessControl.ONLY_ADMINS)
      every { attributesAccessControl } returns MutableLiveData(GroupAccessControl.ONLY_ADMINS)
      every { isAnnouncementGroup } returns MutableLiveData(false)
      every { this@mockk.memberLabelAccessControl } returns MutableLiveData(memberLabelAccessControl)
    }

    every { repository.hasNonAdminMembersWithLabels(groupId) } returns nonAdminMembersHaveLabels

    return PermissionsSettingsViewModel(
      groupId = groupId,
      repository = repository,
      liveGroup = liveGroup
    )
  }

  @Test
  fun `onMemberLabelPermissionChangeRequested immediately applies 'only admins' change when there are no non-admin members with labels`() {
    val viewModel = createViewModel(
      memberLabelAccessControl = GroupAccessControl.ALL_MEMBERS,
      nonAdminMembersHaveLabels = false
    )

    viewModel.onMemberLabelPermissionChangeRequested(nonAdminCanSetMemberLabel = false)

    verify {
      repository.applyMemberLabelRightsChange(
        groupId = groupId,
        newRights = GroupAccessControl.ONLY_ADMINS,
        errorCallback = any()
      )
    }
    LiveDataTestUtil.assertNoValue(viewModel.events)
  }

  @Test
  fun `onMemberLabelPermissionChangeRequested immediately applies 'all members' change when there are no non-admin members with labels`() {
    val viewModel = createViewModel(
      memberLabelAccessControl = GroupAccessControl.ONLY_ADMINS,
      nonAdminMembersHaveLabels = false
    )

    viewModel.onMemberLabelPermissionChangeRequested(nonAdminCanSetMemberLabel = true)

    verify {
      repository.applyMemberLabelRightsChange(
        groupId = groupId,
        newRights = GroupAccessControl.ALL_MEMBERS,
        errorCallback = any()
      )
    }
    LiveDataTestUtil.assertNoValue(viewModel.events)
  }

  @Test
  fun `onMemberLabelPermissionChangeRequested displays warning when restricting to 'only admins' and some non-admin members have labels`() {
    val viewModel = createViewModel(
      memberLabelAccessControl = GroupAccessControl.ALL_MEMBERS,
      nonAdminMembersHaveLabels = true
    )

    viewModel.onMemberLabelPermissionChangeRequested(nonAdminCanSetMemberLabel = false)

    assertEquals(
      PermissionsSettingsEvents.ShowMemberLabelsWillBeRemovedWarning,
      LiveDataTestUtil.observeAndGetOneValue(viewModel.events)
    )

    verify(exactly = 0) {
      repository.applyMemberLabelRightsChange(any(), any(), any())
    }
  }

  @Test
  fun `onMemberLabelPermissionChangeRequested immediately applies 'all members' change when some non-admin members have labels`() {
    val viewModel = createViewModel(memberLabelAccessControl = GroupAccessControl.ALL_MEMBERS, nonAdminMembersHaveLabels = true)

    viewModel.onMemberLabelPermissionChangeRequested(nonAdminCanSetMemberLabel = true)

    verify {
      repository.applyMemberLabelRightsChange(
        groupId = groupId,
        newRights = GroupAccessControl.ALL_MEMBERS,
        errorCallback = any()
      )
    }
    LiveDataTestUtil.assertNoValue(viewModel.events)
  }

  @Test
  fun `onRestrictMemberLabelsToAdminsConfirmed applies 'only admins' change`() {
    val viewModel = createViewModel(memberLabelAccessControl = GroupAccessControl.ALL_MEMBERS, nonAdminMembersHaveLabels = true)

    viewModel.onRestrictMemberLabelsToAdminsConfirmed()

    verify {
      repository.applyMemberLabelRightsChange(
        groupId = groupId,
        newRights = GroupAccessControl.ONLY_ADMINS,
        errorCallback = any()
      )
    }
    LiveDataTestUtil.assertNoValue(viewModel.events)
  }
}
