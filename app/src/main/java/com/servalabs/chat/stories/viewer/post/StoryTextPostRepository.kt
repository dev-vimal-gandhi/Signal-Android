package com.servalabs.chat.stories.viewer.post

import android.graphics.Typeface
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.Base64
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.MmsMessageRecord
import com.servalabs.chat.database.model.databaseprotos.StoryTextPost
import com.servalabs.chat.database.withAttachments
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.fonts.TextFont
import com.servalabs.chat.fonts.TextToScript
import com.servalabs.chat.fonts.TypefaceCache

class StoryTextPostRepository {
  fun getRecord(recordId: Long): Single<MmsMessageRecord> {
    return Single.fromCallable {
      SignalDatabase.messages.getMessageRecord(recordId).withAttachments() as MmsMessageRecord
    }.subscribeOn(Schedulers.io())
  }

  fun getTypeface(recordId: Long): Single<Typeface> {
    return getRecord(recordId).flatMap {
      val model = StoryTextPost.ADAPTER.decode(Base64.decode(it.body))
      val textFont = TextFont.fromStyle(model.style)
      val script = TextToScript.guessScript(model.body)

      TypefaceCache.get(AppDependencies.application, textFont, script)
    }
  }
}
