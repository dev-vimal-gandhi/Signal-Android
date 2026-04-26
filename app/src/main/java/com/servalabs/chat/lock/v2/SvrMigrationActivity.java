package com.servalabs.chat.lock.v2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.servalabs.chat.BaseActivity;
import com.servalabs.chat.PassphrasePromptActivity;
import com.servalabs.chat.R;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.service.KeyCachingService;
import com.servalabs.chat.util.DynamicRegistrationTheme;
import com.servalabs.chat.util.DynamicTheme;

public class SvrMigrationActivity extends BaseActivity {

  public static final int REQUEST_NEW_PIN = CreateSvrPinActivity.REQUEST_NEW_PIN;

  private final DynamicTheme dynamicTheme = new DynamicRegistrationTheme();

  public static Intent createIntent() {
    return new Intent(AppDependencies.getApplication(), SvrMigrationActivity.class);
  }

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    if (KeyCachingService.isLocked(this)) {
      startActivity(getPromptPassphraseIntent());
      finish();
      return;
    }

    dynamicTheme.onCreate(this);

    setContentView(R.layout.kbs_migration_activity);
  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  private Intent getPromptPassphraseIntent() {
    return getRoutedIntent(PassphrasePromptActivity.class, getIntent());
  }

  private Intent getRoutedIntent(Class<?> destination, @Nullable Intent nextIntent) {
    final Intent intent = new Intent(this, destination);
    if (nextIntent != null)   intent.putExtra("next_intent", nextIntent);
    return intent;
  }
}
