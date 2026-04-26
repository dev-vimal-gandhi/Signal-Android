package com.servalabs.chat.avatar.vector

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.servalabs.chat.avatar.Avatar
import com.servalabs.chat.avatar.Avatars
import com.servalabs.chat.util.livedata.Store

class VectorAvatarCreationViewModel(initialAvatar: Avatar.Vector) : ViewModel() {

  private val store = Store(VectorAvatarCreationState(initialAvatar))

  val state: LiveData<VectorAvatarCreationState> = store.stateLiveData

  fun setColor(colorPair: Avatars.ColorPair) {
    store.update { it.copy(currentAvatar = it.currentAvatar.copy(color = colorPair)) }
  }

  fun getCurrentAvatar() = store.state.currentAvatar

  class Factory(private val initialAvatar: Avatar.Vector) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(VectorAvatarCreationViewModel(initialAvatar)))
    }
  }
}
