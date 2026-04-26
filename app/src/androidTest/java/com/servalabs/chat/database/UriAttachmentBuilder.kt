package com.servalabs.chat.database

import android.net.Uri
import org.signal.blurhash.BlurHash
import org.signal.core.models.media.TransformProperties
import com.servalabs.chat.attachments.UriAttachment
import com.servalabs.chat.audio.AudioHash
import com.servalabs.chat.stickers.StickerLocator
import java.util.UUID

object UriAttachmentBuilder {
  fun build(
    id: Long,
    uri: Uri = Uri.parse("content://$id"),
    contentType: String,
    transferState: Int = AttachmentTable.TRANSFER_PROGRESS_PENDING,
    size: Long = 0L,
    fileName: String = "file$id",
    voiceNote: Boolean = false,
    borderless: Boolean = false,
    videoGif: Boolean = false,
    quote: Boolean = false,
    quoteTargetContentType: String? = null,
    caption: String? = null,
    stickerLocator: StickerLocator? = null,
    blurHash: BlurHash? = null,
    audioHash: AudioHash? = null,
    transformProperties: TransformProperties? = null,
    uuid: UUID? = UUID.randomUUID()
  ): UriAttachment {
    return UriAttachment(
      dataUri = uri,
      contentType = contentType,
      transferState = transferState,
      size = size,
      width = 0,
      height = 0,
      fileName = fileName,
      fastPreflightId = null,
      voiceNote = voiceNote,
      borderless = borderless,
      videoGif = videoGif,
      quote = quote,
      quoteTargetContentType = quoteTargetContentType,
      caption = caption,
      stickerLocator = stickerLocator,
      blurHash = blurHash,
      audioHash = audioHash,
      transformProperties = transformProperties,
      uuid = uuid
    )
  }
}
