package com.servalabs.chat.database.loaders;

import android.content.Context;

import com.servalabs.chat.util.AbstractCursorLoader;

public abstract class MediaLoader extends AbstractCursorLoader {

  MediaLoader(Context context) {
    super(context);
  }

  public enum MediaType {
    GALLERY,
    DOCUMENT,
    AUDIO,
    LINK,
    ALL
  }
}
