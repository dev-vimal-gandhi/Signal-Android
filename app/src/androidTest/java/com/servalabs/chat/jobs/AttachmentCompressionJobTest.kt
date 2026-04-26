/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.jobs

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.signal.core.models.media.TransformProperties
import org.signal.core.util.StreamUtil
import com.servalabs.chat.attachments.UriAttachment
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.UriAttachmentBuilder
import com.servalabs.chat.database.transformPropertiesForSentMediaQuality
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.mms.SentMediaQuality
import com.servalabs.chat.providers.BlobProvider
import com.servalabs.chat.testing.SignalActivityRule
import com.servalabs.chat.util.MediaUtil
import java.util.Optional
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AttachmentCompressionJobTest {

  @get:Rule
  val harness = SignalActivityRule()

  @Test
  fun testCompressionJobsWithDifferentTransformPropertiesCompleteSuccessfully() {
    val imageBytes: ByteArray = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("images/sample_image.png").use {
      StreamUtil.readFully(it)
    }

    val blob = BlobProvider.getInstance().forData(imageBytes).createForSingleSessionOnDisk(AppDependencies.application)

    val firstPreUpload = createAttachment(1, blob, TransformProperties.empty())
    val firstDatabaseAttachment = SignalDatabase.attachments.insertAttachmentForPreUpload(firstPreUpload)

    val firstCompressionJob: AttachmentCompressionJob = AttachmentCompressionJob.fromAttachment(firstDatabaseAttachment, false, -1)

    var secondCompressionJob: AttachmentCompressionJob? = null
    var firstJobResult: Job.Result? = null
    var secondJobResult: Job.Result? = null

    val secondJobLatch = CountDownLatch(1)
    val jobThread = Thread {
      firstCompressionJob.setContext(AppDependencies.application)
      firstJobResult = firstCompressionJob.run()

      secondJobLatch.await()

      secondCompressionJob!!.setContext(AppDependencies.application)
      secondJobResult = secondCompressionJob!!.run()
    }

    jobThread.start()
    val secondPreUpload = createAttachment(1, blob, transformPropertiesForSentMediaQuality(Optional.empty(), SentMediaQuality.HIGH))
    val secondDatabaseAttachment = SignalDatabase.attachments.insertAttachmentForPreUpload(secondPreUpload)
    secondCompressionJob = AttachmentCompressionJob.fromAttachment(secondDatabaseAttachment, false, -1)

    secondJobLatch.countDown()

    jobThread.join()

    assertThat(firstJobResult!!.isSuccess).isTrue()
    assertThat(secondJobResult!!.isSuccess).isTrue()
  }

  private fun createAttachment(id: Long, uri: Uri, transformProperties: TransformProperties): UriAttachment {
    return UriAttachmentBuilder.build(
      id,
      uri = uri,
      contentType = MediaUtil.IMAGE_JPEG,
      transformProperties = transformProperties
    )
  }
}
