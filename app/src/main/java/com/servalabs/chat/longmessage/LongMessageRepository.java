package com.servalabs.chat.longmessage;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.Log;
import com.servalabs.chat.conversation.ConversationMessage;
import com.servalabs.chat.database.MessageTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.model.MessageRecord;
import com.servalabs.chat.database.model.MmsMessageRecord;

import java.util.Optional;

class LongMessageRepository {

  private final static String TAG = Log.tag(LongMessageRepository.class);

  private final MessageTable messageTable;

  LongMessageRepository() {
    this.messageTable = SignalDatabase.messages();
  }

  void getMessage(@NonNull Context context, long messageId, @NonNull Callback<Optional<LongMessage>> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      callback.onComplete(getMmsLongMessage(context, messageTable, messageId));
    });
  }

  @WorkerThread
  private Optional<LongMessage> getMmsLongMessage(@NonNull Context context, @NonNull MessageTable mmsDatabase, long messageId) {
    Optional<MmsMessageRecord> record = getMmsMessage(mmsDatabase, messageId);
    if (record.isPresent()) {
      final ConversationMessage resolvedMessage = LongMessageResolveerKt.resolveBody(record.get(), context);
      return  Optional.of(new LongMessage(resolvedMessage));
    } else {
      return Optional.empty();
    }
  }

  @WorkerThread
  private Optional<MmsMessageRecord> getMmsMessage(@NonNull MessageTable mmsDatabase, long messageId) {
    try (Cursor cursor = mmsDatabase.getMessageCursor(messageId)) {
      MessageRecord record = MessageTable.mmsReaderFor(cursor).getNext();
      if (record != null) {
        record = MessageTable.withAttachmentData(record);
      }
      return Optional.ofNullable((MmsMessageRecord) record);
    }
  }


  interface Callback<T> {
    void onComplete(T result);
  }
}
