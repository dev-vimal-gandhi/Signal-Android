package com.servalabs.chat.conversation;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.signal.core.util.StreamUtil;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.Log;
import com.servalabs.chat.database.MessageTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.ThreadTable;
import com.servalabs.chat.database.model.GroupRecord;
import com.servalabs.chat.database.model.MessageId;
import com.servalabs.chat.database.model.MessageRecord;
import com.servalabs.chat.database.model.MmsMessageRecord;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.MultiDeviceViewedUpdateJob;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.mms.PartAuthority;
import com.servalabs.chat.mms.TextSlide;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.recipients.RecipientUtil;
import com.servalabs.chat.util.MessageRecordUtil;
import org.signal.core.models.ServiceId;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConversationRepository {

  private static final String TAG = Log.tag(ConversationRepository.class);

  private final Context context;

  public ConversationRepository() {
    this.context = AppDependencies.getApplication();
  }

  @WorkerThread
  public @NonNull ConversationData getConversationData(long threadId, @NonNull Recipient conversationRecipient, int jumpToPosition) {
    ThreadTable.ConversationMetadata    metadata                       = SignalDatabase.threads().getConversationMetadata(threadId);
    int                                 threadSize                     = SignalDatabase.messages().getMessageCountForThread(threadId);
    long                                lastSeen                       = metadata.getLastSeen();
    int                                 lastSeenPosition               = 0;
    long                                lastScrolled                   = metadata.getLastScrolled();
    int                                 lastScrolledPosition           = 0;
    boolean                             isMessageRequestAccepted       = RecipientUtil.isMessageRequestAccepted(threadId);
    boolean                             isConversationHidden           = RecipientUtil.isRecipientHidden(threadId);
    ConversationData.MessageRequestData messageRequestData             = new ConversationData.MessageRequestData(isMessageRequestAccepted, isConversationHidden);
    boolean                             showUniversalExpireTimerUpdate = false;

    if (lastSeen > 0) {
      lastSeenPosition = SignalDatabase.messages().getMessagePositionByDateReceivedTimestamp(threadId, lastSeen, false);
    }

    if (lastSeenPosition <= 0) {
      lastSeen = 0;
    }

    if (lastSeen == 0 && lastScrolled > 0) {
      lastScrolledPosition = SignalDatabase.messages().getMessagePositionByDateReceivedTimestamp(threadId, lastScrolled, true);
    }

    if (!isMessageRequestAccepted) {
      boolean isGroup                             = false;
      boolean recipientIsKnownOrHasGroupsInCommon = false;
      if (conversationRecipient.isGroup()) {
        Optional<GroupRecord> group = SignalDatabase.groups().getGroup(conversationRecipient.getId());
        if (group.isPresent()) {
          List<Recipient> recipients = Recipient.resolvedList(group.get().getMembers());
          for (Recipient recipient : recipients) {
            if ((recipient.isProfileSharing() || recipient.getHasGroupsInCommon()) && !recipient.isSelf()) {
              recipientIsKnownOrHasGroupsInCommon = true;
              break;
            }
          }
        }
        isGroup = true;
      } else if (conversationRecipient.getHasGroupsInCommon()) {
        recipientIsKnownOrHasGroupsInCommon = true;
      }
      messageRequestData = new ConversationData.MessageRequestData(isMessageRequestAccepted, isConversationHidden, recipientIsKnownOrHasGroupsInCommon, isGroup);
    }

    List<ServiceId> groupMemberAcis;
    if (conversationRecipient.isPushV2Group()) {
      groupMemberAcis = conversationRecipient.getParticipantAcis();
    } else {
      groupMemberAcis = Collections.emptyList();
    }

    if (SignalStore.settings().getUniversalExpireTimer() != 0 &&
        conversationRecipient.getExpiresInSeconds() == 0 &&
        !conversationRecipient.isGroup() &&
        conversationRecipient.isRegistered() &&
        SignalDatabase.messages().canSetUniversalTimer(threadId))
    {
      showUniversalExpireTimerUpdate = true;
    }

    return new ConversationData(conversationRecipient, threadId, lastSeen, lastSeenPosition, lastScrolledPosition, jumpToPosition, threadSize, messageRequestData, showUniversalExpireTimerUpdate, metadata.getUnreadCount(), groupMemberAcis);
  }

  public void markGiftBadgeRevealed(long messageId) {
    SignalExecutors.BOUNDED_IO.execute(() -> {
      List<MessageTable.MarkedMessageInfo> markedMessageInfo = SignalDatabase.messages().setOutgoingGiftsRevealed(Collections.singletonList(messageId));
      if (!markedMessageInfo.isEmpty()) {
        Log.d(TAG, "Marked gift badge revealed. Sending view sync message.");
        MultiDeviceViewedUpdateJob.enqueue(
            markedMessageInfo.stream()
                             .map(MessageTable.MarkedMessageInfo::getSyncMessageId)
                             .collect(Collectors.toList()));
      }
    });
  }

  @NonNull
  public Single<ConversationMessage> resolveMessageToEdit(@NonNull ConversationMessage message) {
    return Single.fromCallable(() -> {
                   ConversationMessage latestMessage = message;
                   MessageRecord       messageRecord = latestMessage.getMessageRecord();

                   MessageId latestRevisionId = messageRecord.isMms() ? ((MmsMessageRecord) messageRecord).getLatestRevisionId() : null;
                   if (latestRevisionId != null) {
                     MessageRecord latestRecord = SignalDatabase.messages().getMessageRecordOrNull(latestRevisionId.getId());
                     if (latestRecord != null) {
                       Log.e(TAG, "Resolving edit to latest revision: " + latestRevisionId.getId() + " (was: " + messageRecord.getId() + ")");
                       messageRecord = latestRecord;
                       latestMessage = ConversationMessage.ConversationMessageFactory.createWithUnresolvedData(context, messageRecord, messageRecord.getDisplayBody(context).toString(), message.getThreadRecipient());
                     }
                   }

                   if (MessageRecordUtil.hasTextSlide(messageRecord)) {
                     TextSlide textSlide = MessageRecordUtil.requireTextSlide(messageRecord);
                     if (textSlide.getUri() == null) {
                       return latestMessage;
                     }

                     try (InputStream stream = PartAuthority.getAttachmentStream(context, textSlide.getUri())) {
                       String body = StreamUtil.readFullyAsString(stream);
                       return ConversationMessage.ConversationMessageFactory.createWithUnresolvedData(context, messageRecord, body, message.getThreadRecipient());
                     } catch (IOException e) {
                       Log.w(TAG, "Failed to read text slide data.");
                     }
                   }
                   return latestMessage;
                 }).subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread());
  }
}
