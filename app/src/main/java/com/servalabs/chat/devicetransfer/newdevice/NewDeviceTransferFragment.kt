package com.servalabs.chat.devicetransfer.newdevice

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.signal.devicetransfer.DeviceToDeviceTransferService
import com.servalabs.chat.R
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.restore.RestoreActivity
import com.servalabs.chat.restore.devicetransfer.DeviceTransferFragment
import com.servalabs.chat.util.navigation.safeNavigate

/**
 * Shows transfer progress on the new device. Most logic is in [DeviceTransferFragment]
 * and it delegates to this class for strings, navigation, and updating progress.
 */
class NewDeviceTransferFragment : DeviceTransferFragment() {

  private val viewModel: NewDeviceTransferViewModel by viewModels()
  private val serverTaskListener = ServerTaskListener()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    EventBus.getDefault().register(serverTaskListener)
  }

  override fun onDestroyView() {
    EventBus.getDefault().unregister(serverTaskListener)
    super.onDestroyView()
  }

  override fun navigateToRestartTransfer() {
    findNavController().safeNavigate(NewDeviceTransferFragmentDirections.actionNewDeviceTransferToNewDeviceTransferInstructions())
  }

  override fun navigateAwayFromTransfer() {
    EventBus.getDefault().unregister(serverTaskListener)
    requireActivity().finish()
  }

  override fun navigateToTransferComplete() {
    if (SignalStore.account.isRegistered) {
      (requireActivity() as RestoreActivity).onBackupCompletedSuccessfully()
    } else {
      findNavController().safeNavigate(NewDeviceTransferFragmentDirections.actionNewDeviceTransferToNewDeviceTransferComplete())
    }
  }

  private fun onRestoreComplete() {
    ignoreTransferStatusEvents()
    DeviceToDeviceTransferService.stop(requireContext())

    viewModel.onRestoreComplete(requireContext()) {
      transferFinished = true
      navigateToTransferComplete()
    }
  }

  private inner class ServerTaskListener {
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NewDeviceServerTask.Status) {
      status.text = getString(R.string.DeviceTransfer__d_messages_so_far, event.messageCount)

      when (event.state) {
        NewDeviceServerTask.Status.State.IN_PROGRESS,
        NewDeviceServerTask.Status.State.TRANSFER_COMPLETE -> Unit

        NewDeviceServerTask.Status.State.RESTORE_COMPLETE -> onRestoreComplete()
        NewDeviceServerTask.Status.State.FAILURE_VERSION_DOWNGRADE -> abort(R.string.NewDeviceTransfer__cannot_transfer_from_a_newer_version_of_signal)
        NewDeviceServerTask.Status.State.FAILURE_FOREIGN_KEY -> abort(R.string.NewDeviceTransfer__failure_foreign_key)
        NewDeviceServerTask.Status.State.FAILURE_UNKNOWN -> abort()
      }
    }
  }
}
