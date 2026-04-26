package com.servalabs.chat.scribbles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.signal.core.util.concurrent.SignalExecutors;
import com.servalabs.chat.R;
import com.servalabs.chat.components.emoji.MediaKeyboard;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.model.StickerRecord;
import com.servalabs.chat.keyboard.KeyboardPage;
import com.servalabs.chat.keyboard.sticker.StickerKeyboardPageFragment;
import com.servalabs.chat.keyboard.sticker.StickerSearchDialogFragment;
import com.servalabs.chat.scribbles.stickers.FeatureSticker;
import com.servalabs.chat.scribbles.stickers.ScribbleStickersFragment;
import com.servalabs.chat.stickers.StickerEventListener;
import com.servalabs.chat.stickers.manage.StickerManagementScreen;
import com.servalabs.chat.util.ViewUtil;

public final class ImageEditorStickerSelectActivity extends AppCompatActivity implements StickerEventListener, MediaKeyboard.MediaKeyboardListener, StickerKeyboardPageFragment.Callback, ScribbleStickersFragment.Callback {

  public static final String EXTRA_FEATURE_STICKER = "imageEditor.featureSticker";

  @Override
  protected void attachBaseContext(@NonNull Context newBase) {
    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    super.attachBaseContext(newBase);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scribble_select_new_sticker_activity);
  }

  @Override
  public void onShown() {
  }

  @Override
  public void onHidden() {
    finish();
  }

  @Override
  public void onKeyboardChanged(@NonNull KeyboardPage page) {
  }

  @Override
  public void onStickerSelected(@NonNull StickerRecord sticker) {
    Intent intent = new Intent();
    intent.setData(sticker.uri);
    setResult(RESULT_OK, intent);

    SignalExecutors.BOUNDED.execute(() -> SignalDatabase.stickers().updateStickerLastUsedTime(sticker.rowId, System.currentTimeMillis()));
    ViewUtil.hideKeyboard(this, findViewById(android.R.id.content));
    finish();
  }

  @Override
  public void onStickerManagementClicked() {
    StickerManagementScreen.show(this);
  }

  @Override
  public void openStickerSearch() {
    StickerSearchDialogFragment.show(getSupportFragmentManager());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onFeatureSticker(FeatureSticker featureSticker) {
    Intent intent = new Intent();
    intent.putExtra(EXTRA_FEATURE_STICKER, featureSticker.getType());
    setResult(RESULT_OK, intent);

    ViewUtil.hideKeyboard(this, findViewById(android.R.id.content));
    finish();
  }
}
