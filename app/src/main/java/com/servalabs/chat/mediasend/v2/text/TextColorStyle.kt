package com.servalabs.chat.mediasend.v2.text

import androidx.annotation.DrawableRes
import com.servalabs.chat.R

enum class TextColorStyle(@DrawableRes val icon: Int) {
  /**
   * Transparent background.
   */
  NO_BACKGROUND(R.drawable.ic_text_normal),

  /**
   * White background, textColor foreground.
   */
  NORMAL(R.drawable.ic_text_effect),

  /**
   * textColor background with white foreground.
   */
  INVERT(R.drawable.ic_text_effect)
}
