package com.servalabs.chat.mms

import android.content.ContentUris
import android.net.Uri
import com.servalabs.chat.attachments.AttachmentId

/**
 * Parses the given [Uri] into either an [AttachmentId] or a [Long]
 */
class PartUriParser(private val uri: Uri) {
  val partId: AttachmentId
    get() = AttachmentId(id)

  private val id: Long
    get() = ContentUris.parseId(uri)
}
