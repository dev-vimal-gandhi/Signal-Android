package org.signal.camera.demo

import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import com.servalabs.chat.mms.RegisterGlideComponents
import com.servalabs.chat.mms.SignalGlideModule

/**
 * Application class for the camera demo.
 */
class CameraDemoApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    Log.initialize(AndroidLogger)
    SignalGlideModule.registerGlideComponents = object : RegisterGlideComponents {
      override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
      }
    }
  }
}
