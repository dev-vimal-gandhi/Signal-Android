package com.servalabs.chat.recipients.ui.bottomsheet;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import org.signal.core.models.ServiceId;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.concurrent.SimpleTask;
import org.signal.core.util.logging.Log;
import com.servalabs.chat.contacts.sync.ContactDiscovery;
import com.servalabs.chat.database.GroupTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.model.GroupRecord;
import com.servalabs.chat.database.model.IdentityRecord;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.groups.GroupChangeException;
import com.servalabs.chat.groups.GroupId;
import com.servalabs.chat.groups.GroupManager;
import com.servalabs.chat.groups.ui.GroupChangeErrorCallback;
import com.servalabs.chat.groups.ui.GroupChangeFailureReason;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.recipients.RecipientId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class RecipientDialogRepository {

  private static final String TAG = Log.tag(RecipientDialogRepository.class);

  @NonNull  private final Context     context;
  @NonNull  private final RecipientId recipientId;
  @Nullable private final GroupId     groupId;

  RecipientDialogRepository(@NonNull Context context,
                            @NonNull RecipientId recipientId,
                            @Nullable GroupId groupId)
  {
    this.context     = context;
    this.recipientId = recipientId;
    this.groupId     = groupId;
  }

  @NonNull RecipientId getRecipientId() {
    return recipientId;
  }

  @Nullable GroupId getGroupId() {
    return groupId;
  }

  void getIdentity(@NonNull Consumer<IdentityRecord> callback) {
    SignalExecutors.BOUNDED.execute(
      () -> callback.accept(AppDependencies.getProtocolStore().aci().identities().getIdentityRecord(recipientId).orElse(null)));
  }

  void getRecipient(@NonNull RecipientCallback recipientCallback) {
    SimpleTask.run(SignalExecutors.BOUNDED,
                   () -> Recipient.resolved(recipientId),
                   recipientCallback::onRecipient);
  }

  void refreshRecipient() {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        ContactDiscovery.refresh(context, Recipient.resolved(recipientId), false);
      } catch (IOException e) {
        Log.w(TAG, "Failed to refresh user after adding to contacts.");
      }
    });
  }

  void removeMember(@NonNull Consumer<Boolean> onComplete, @NonNull GroupChangeErrorCallback error) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     try {
                       GroupManager.ejectAndBanFromGroup(context, Objects.requireNonNull(groupId).requireV2(), Recipient.resolved(recipientId));
                       return true;
                     } catch (GroupChangeException | IOException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.fromException(e));
                     }
                     return false;
                   },
                   onComplete::accept);
  }

  void setMemberAdmin(boolean admin, @NonNull Consumer<Boolean> onComplete, @NonNull GroupChangeErrorCallback error) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     try {
                       GroupManager.setMemberAdmin(context, Objects.requireNonNull(groupId).requireV2(), recipientId, admin);
                       return true;
                     } catch (GroupChangeException | IOException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.fromException(e));
                     }
                     return false;
                   },
                   onComplete::accept);
  }

  void willAdminDemotionClearLabel(@NonNull Consumer<Boolean> onComplete) {
    SimpleTask.BackgroundTask<Boolean> hasLabelToClear = () -> {
      if (groupId == null || !groupId.isV2()) {
        return false;
      }

      GroupRecord   groupRecord = SignalDatabase.groups().getGroup(groupId.requireV2()).orElse(null);
      ServiceId.ACI aci         = Recipient.resolved(recipientId).getAci().orElse(null);

      if (groupRecord != null && aci != null) {
        return groupRecord.requireV2GroupProperties().adminDemotionClearsLabel(aci);
      }
      return false;
    };

    SimpleTask.run(SignalExecutors.UNBOUNDED, hasLabelToClear, onComplete::accept);
  }

  void getGroupMembership(@NonNull Consumer<List<RecipientId>> onComplete) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     GroupTable             groupDatabase   = SignalDatabase.groups();
                     List<GroupRecord>      groupRecords    = groupDatabase.getPushGroupsContainingMember(recipientId);
                     ArrayList<RecipientId> groupRecipients = new ArrayList<>(groupRecords.size());

                     for (GroupRecord groupRecord : groupRecords) {
                       groupRecipients.add(groupRecord.getRecipientId());
                     }

                     return groupRecipients;
                   },
                   onComplete::accept);
  }

  public void getActiveGroupCount(@NonNull Consumer<Integer> onComplete) {
    SignalExecutors.BOUNDED.execute(() -> onComplete.accept(SignalDatabase.groups().getActiveGroupCount()));
  }

  interface RecipientCallback {
    void onRecipient(@NonNull Recipient recipient);
  }
}
