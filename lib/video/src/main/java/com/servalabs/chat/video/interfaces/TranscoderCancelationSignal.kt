package com.servalabs.chat.video.interfaces

fun interface TranscoderCancelationSignal {
  fun isCanceled(): Boolean
}
