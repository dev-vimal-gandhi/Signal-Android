package com.servalabs.chat.components.webrtc;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.util.Consumer;

import org.signal.core.util.concurrent.SignalExecutors;
import com.servalabs.chat.database.GroupTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.identity.IdentityRecordList;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.recipients.Recipient;

import java.util.Collections;
import java.util.List;

public final class WebRtcCallRepository {

  private WebRtcCallRepository() {}

  @WorkerThread
  public static void getIdentityRecords(@NonNull Recipient recipient, @NonNull Consumer<IdentityRecordList> consumer) {
    SignalExecutors.BOUNDED.execute(() -> {
      List<Recipient> recipients;

      if (recipient.isGroup()) {
        recipients = SignalDatabase.groups().getGroupMembers(recipient.requireGroupId(), GroupTable.MemberSet.FULL_MEMBERS_EXCLUDING_SELF);
      } else {
        recipients = Collections.singletonList(recipient);
      }

      consumer.accept(AppDependencies.getProtocolStore().aci().identities().getIdentityRecords(recipients));
    });
  }
}
