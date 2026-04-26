package com.servalabs.chat.mediasend.v2

import android.view.animation.Interpolator
import com.servalabs.chat.util.createDefaultCubicBezierInterpolator

object MediaAnimations {
  /**
   * Fast-In-Extra-Slow-Out Interpolator
   */
  @JvmStatic
  val interpolator: Interpolator = createDefaultCubicBezierInterpolator()
}
