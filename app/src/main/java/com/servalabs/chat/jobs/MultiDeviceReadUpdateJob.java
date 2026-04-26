package com.servalabs.chat.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.signal.core.util.ListUtil;
import org.signal.core.util.logging.Log;
import com.servalabs.chat.database.MessageTable.SyncMessageId;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.JobManager;
import com.servalabs.chat.jobmanager.JsonJobData;
import com.servalabs.chat.jobmanager.impl.NetworkConstraint;
import com.servalabs.chat.jobmanager.impl.SealedSenderConstraint;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.net.NotPushRegisteredException;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.recipients.RecipientId;
import com.servalabs.chat.recipients.RecipientUtil;
import com.servalabs.chat.util.JsonUtils;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.signal.core.models.ServiceId;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.push.exceptions.ServerRejectedException;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MultiDeviceReadUpdateJob extends BaseJob {

  public static final String KEY = "MultiDeviceReadUpdateJob";

  private static final String TAG = Log.tag(MultiDeviceReadUpdateJob.class);

  private static final String KEY_MESSAGE_IDS = "message_ids";

  private List<SerializableSyncMessageId> messageIds;

  private MultiDeviceReadUpdateJob(List<SyncMessageId> messageIds) {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .addConstraint(SealedSenderConstraint.KEY)
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build(),
         SendReadReceiptJob.ensureSize(messageIds, SendReadReceiptJob.MAX_TIMESTAMPS));
  }

  private MultiDeviceReadUpdateJob(@NonNull Job.Parameters parameters, @NonNull List<SyncMessageId> messageIds) {
    super(parameters);

    this.messageIds = new LinkedList<>();

    for (SyncMessageId messageId : messageIds) {
      this.messageIds.add(new SerializableSyncMessageId(messageId.getRecipientId().serialize(), messageId.getTimetamp()));
    }
  }

  /**
   * Enqueues all the necessary jobs for read receipts, ensuring that they're all within the
   * maximum size.
   */
  public static void enqueue(@NonNull List<SyncMessageId> messageIds) {
    JobManager                jobManager      = AppDependencies.getJobManager();
    List<List<SyncMessageId>> messageIdChunks = ListUtil.chunk(messageIds, SendReadReceiptJob.MAX_TIMESTAMPS);

    if (messageIdChunks.size() > 1) {
      Log.w(TAG, "Large receipt count! Had to break into multiple chunks. Total count: " + messageIds.size());
    }

    for (List<SyncMessageId> chunk : messageIdChunks) {
      jobManager.add(new MultiDeviceReadUpdateJob(chunk));
    }
  }

  @Override
  public @Nullable byte[] serialize() {
    String[] ids = new String[messageIds.size()];

    for (int i = 0; i < ids.length; i++) {
      try {
        ids[i] = JsonUtils.toJson(messageIds.get(i));
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }

    return new JsonJobData.Builder().putStringArray(KEY_MESSAGE_IDS, ids).serialize();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, UntrustedIdentityException {
    if (!Recipient.self().isRegistered()) {
      throw new NotPushRegisteredException();
    }

    if (!SignalStore.account().isMultiDevice()) {
      Log.i(TAG, "Not multi device...");
      return;
    }

    List<ReadMessage> readMessages = new LinkedList<>();

    for (SerializableSyncMessageId messageId : messageIds) {
      Recipient recipient = Recipient.resolved(RecipientId.from(messageId.recipientId));
      if (!recipient.isGroup() && !recipient.isDistributionList() && recipient.isMaybeRegistered() && (recipient.getHasServiceId() || recipient.getHasE164())) {
        ServiceId senderAci = RecipientUtil.getOrFetchServiceId(context, recipient);
        if (senderAci instanceof ServiceId.ACI) {
          readMessages.add(new ReadMessage((ServiceId.ACI) senderAci, messageId.timestamp));
        } else {
          Log.w(TAG, "Failed to add ReadMessage for sender without an ACI! { recipientId: " + messageId.recipientId + ", timestamp: " + messageId.timestamp + " }");
        }
      }
    }

    SignalServiceMessageSender messageSender = AppDependencies.getSignalServiceMessageSender();
    messageSender.sendSyncMessage(SignalServiceSyncMessage.forRead(readMessages));
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof ServerRejectedException) return false;
    return exception instanceof PushNetworkException;
  }

  @Override
  public void onFailure() {

  }

  private static class SerializableSyncMessageId implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final String recipientId;

    @JsonProperty
    private final long   timestamp;

    private SerializableSyncMessageId(@JsonProperty("recipientId") String recipientId, @JsonProperty("timestamp") long timestamp) {
      this.recipientId = recipientId;
      this.timestamp   = timestamp;
    }
  }

  public static final class Factory implements Job.Factory<MultiDeviceReadUpdateJob> {
    @Override
    public @NonNull MultiDeviceReadUpdateJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      JsonJobData data = JsonJobData.deserialize(serializedData);

      List<SyncMessageId> ids = Stream.of(data.getStringArray(KEY_MESSAGE_IDS))
                                      .map(id -> {
                                        try {
                                          return JsonUtils.fromJson(id, SerializableSyncMessageId.class);
                                        } catch (IOException e) {
                                          throw new AssertionError(e);
                                        }
                                      })
                                      .map(id -> new SyncMessageId(RecipientId.from(id.recipientId), id.timestamp)).collect(Collectors.toList());

      return new MultiDeviceReadUpdateJob(parameters, ids);
    }
  }
}
