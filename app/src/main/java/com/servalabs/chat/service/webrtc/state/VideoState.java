package com.servalabs.chat.service.webrtc.state;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.servalabs.chat.components.webrtc.BroadcastVideoSink;
import com.servalabs.chat.components.webrtc.EglBaseWrapper;
import com.servalabs.chat.ringrtc.Camera;

import java.util.Objects;

/**
 * Local device video state and infrastructure.
 */
public final class VideoState {
  EglBaseWrapper     eglBase;
  BroadcastVideoSink localSink;
  Camera             camera;

  VideoState() {
    this(null, null, null);
  }

  VideoState(@NonNull VideoState toCopy) {
    this(toCopy.eglBase, toCopy.localSink, toCopy.camera);
  }

  VideoState(@NonNull EglBaseWrapper eglBase, @Nullable BroadcastVideoSink localSink, @Nullable Camera camera) {
    this.eglBase   = eglBase;
    this.localSink = localSink;
    this.camera    = camera;
  }

  public @NonNull EglBaseWrapper getLockableEglBase() {
    return eglBase;
  }

  public @Nullable BroadcastVideoSink getLocalSink() {
    return localSink;
  }

  public @NonNull BroadcastVideoSink requireLocalSink() {
    return Objects.requireNonNull(localSink);
  }

  public @Nullable Camera getCamera() {
    return camera;
  }

  public @NonNull Camera requireCamera() {
    return Objects.requireNonNull(camera);
  }
}
