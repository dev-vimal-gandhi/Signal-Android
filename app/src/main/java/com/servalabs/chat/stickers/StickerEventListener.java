package com.servalabs.chat.stickers;

import androidx.annotation.NonNull;

import com.servalabs.chat.database.model.StickerRecord;

public interface StickerEventListener {
  void onStickerSelected(@NonNull StickerRecord sticker);

  void onStickerManagementClicked();
}
