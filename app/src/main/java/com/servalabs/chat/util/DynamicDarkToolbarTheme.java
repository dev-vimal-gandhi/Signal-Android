package com.servalabs.chat.util;

import androidx.annotation.StyleRes;

import com.servalabs.chat.R;

public class DynamicDarkToolbarTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_DarkNoActionBar;
  }
}
