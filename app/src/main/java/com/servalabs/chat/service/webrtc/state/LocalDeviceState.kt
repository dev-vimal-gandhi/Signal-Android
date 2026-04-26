package com.servalabs.chat.service.webrtc.state

import com.servalabs.chat.components.sensors.Orientation
import com.servalabs.chat.events.CallParticipant
import com.servalabs.chat.ringrtc.CameraState
import com.servalabs.chat.webrtc.audio.SignalAudioManager
import org.webrtc.PeerConnection

/**
 * Local device specific state.
 */
data class LocalDeviceState(
  var cameraState: CameraState = CameraState.UNKNOWN,
  var isMicrophoneEnabled: Boolean = true,
  var orientation: Orientation = Orientation.PORTRAIT_BOTTOM_EDGE,
  var isLandscapeEnabled: Boolean = false,
  var deviceOrientation: Orientation = Orientation.PORTRAIT_BOTTOM_EDGE,
  var activeDevice: SignalAudioManager.AudioDevice = SignalAudioManager.AudioDevice.NONE,
  var availableDevices: Set<SignalAudioManager.AudioDevice> = emptySet(),
  var bluetoothPermissionDenied: Boolean = false,
  var isAudioDeviceChangePending: Boolean = false,
  var networkConnectionType: PeerConnection.AdapterType = PeerConnection.AdapterType.UNKNOWN,
  var handRaisedTimestamp: Long = CallParticipant.HAND_LOWERED,
  var remoteMutedBy: CallParticipant? = null
) {

  fun duplicate(): LocalDeviceState {
    return copy()
  }
}
