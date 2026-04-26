package com.servalabs.chat.conversation.v2

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.enableSavedStateHandles
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.signal.core.util.logging.Log
import org.signal.core.util.logging.Log.tag
import com.servalabs.chat.MainActivity
import com.servalabs.chat.PassphraseRequiredActivity
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.app.subscription.GooglePayComponent
import com.servalabs.chat.components.settings.app.subscription.GooglePayRepository
import com.servalabs.chat.components.voice.VoiceNoteMediaController
import com.servalabs.chat.components.voice.VoiceNoteMediaControllerOwner
import com.servalabs.chat.conversation.ConversationIntents
import com.servalabs.chat.jobs.ConversationShortcutUpdateJob
import com.servalabs.chat.util.ConfigurationUtil
import com.servalabs.chat.util.Debouncer
import com.servalabs.chat.util.DynamicNoActionBarTheme
import java.util.concurrent.TimeUnit

/**
 * Wrapper activity for ConversationFragment.
 */
open class ConversationActivity : PassphraseRequiredActivity(), VoiceNoteMediaControllerOwner, GooglePayComponent {

  companion object {
    private val TAG = tag(ConversationActivity::class.java)

    private const val STATE_WATERMARK = "share_data_watermark"
  }

  private val theme = DynamicNoActionBarTheme()
  private val transitionDebouncer: Debouncer = Debouncer(150, TimeUnit.MILLISECONDS)

  override val voiceNoteMediaController = VoiceNoteMediaController(this, true)

  override val googlePayRepository: GooglePayRepository by lazy { GooglePayRepository(this) }
  override val googlePayResultPublisher: Subject<GooglePayComponent.GooglePayResult> = PublishSubject.create()

  private val motionEventRelay: MotionEventRelay by viewModels()
  private val shareDataTimestampViewModel: ShareDataTimestampViewModel by viewModels()

  override fun onPreCreate() {
    theme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    if (!ActivityCompat.isLaunchedFromBubble(this)) {
      startActivity(
        MainActivity.clearTop(this).apply {
          action = ConversationIntents.ACTION
          putExtras(intent)
        }
      )

      if (!ConversationIntents.isConversationIntent(intent)) {
        ConversationShortcutUpdateJob.enqueue()
      }

      finish()
      return
    }

    enableSavedStateHandles()
    supportPostponeEnterTransition()
    transitionDebouncer.publish { supportStartPostponedEnterTransition() }
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

    shareDataTimestampViewModel.setTimestampFromActivityCreation(savedInstanceState, intent)
    setContentView(R.layout.fragment_container)

    if (savedInstanceState == null) {
      replaceFragment()
    }
  }

  override fun onResume() {
    super.onResume()
    theme.onResume(this)
  }

  override fun onStop() {
    super.onStop()
    if (isChangingConfigurations) {
      Log.i(TAG, "Conversation recreating due to configuration change")
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    transitionDebouncer.clear()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    // Note: We utilize this instead of 'replaceFragment' because there seems to be a bug
    // in constraint-layout which mixes up insets when replacing the fragment via onNewIntent.
    finish()
    startActivity(intent)
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayResultPublisher.onNext(GooglePayComponent.GooglePayResult(requestCode, resultCode, data))
  }

  override fun onConfigurationChanged(newConfiguration: Configuration) {
    super.onConfigurationChanged(newConfiguration)
    if (ConfigurationUtil.isUiModeChanged(resources.configuration, newConfiguration)) {
      recreate()
    }
  }

  private fun replaceFragment() {
    val fragment = ConversationFragment().apply {
      arguments = if (ConversationIntents.isBubbleIntentUri(intent.data)) {
        ConversationIntents.createParentFragmentArguments(intent)
      } else {
        intent.extras
      }
    }

    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .disallowAddToBackStack()
      .commitNowAllowingStateLoss()
  }

  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    return motionEventRelay.offer(ev) || super.dispatchTouchEvent(ev)
  }
}
