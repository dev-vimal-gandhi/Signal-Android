package com.servalabs.chat.service.webrtc

import org.signal.core.models.ServiceId.ACI
import org.signal.ringrtc.CallManager
import com.servalabs.chat.groups.GroupId
import com.servalabs.chat.recipients.RecipientId

data class GroupCallRingCheckInfo(
  val recipientId: RecipientId,
  val groupId: GroupId.V2,
  val ringId: Long,
  val ringerAci: ACI,
  val ringUpdate: CallManager.RingUpdate
)
