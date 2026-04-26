package com.servalabs.chat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.conversation.ConversationIntents;
import com.servalabs.chat.conversation.NewConversationActivity;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.util.Rfc5724Uri;

import java.net.URISyntaxException;

public class SystemContactsEntrypointActivity extends Activity {

  private static final String TAG = Log.tag(SystemContactsEntrypointActivity.class);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    startActivity(getNextIntent(getIntent()));
    finish();
    super.onCreate(savedInstanceState);
  }

  private Intent getNextIntent(Intent original) {
    DestinationAndBody destination;

    if (original.getData() != null && "content".equals(original.getData().getScheme())) {
      destination = getDestinationForSyncAdapter(original);
    } else {
      destination = getDestinationForView(original);
    }

    final Intent nextIntent;

    if (TextUtils.isEmpty(destination.destination)) {
      nextIntent = NewConversationActivity.createIntent(this, destination.getBody());
      Toast.makeText(this, R.string.ConversationActivity_specify_recipient, Toast.LENGTH_LONG).show();
    } else {
      Recipient recipient = Recipient.external(destination.getDestination());

      if (recipient != null) {
        long threadId = SignalDatabase.threads().getOrCreateThreadIdFor(recipient);

        nextIntent = ConversationIntents.createBuilderSync(this, recipient.getId(), threadId)
                                        .withDraftText(destination.getBody())
                                        .build();
      } else {
        nextIntent = NewConversationActivity.createIntent(this, destination.getBody());
        Toast.makeText(this, R.string.ConversationActivity_specify_recipient, Toast.LENGTH_LONG).show();
      }
    }
    return nextIntent;
  }

  private @NonNull DestinationAndBody getDestinationForView(Intent intent) {
    try {
      Rfc5724Uri smsUri = new Rfc5724Uri(intent.getData().toString());
      return new DestinationAndBody(smsUri.getPath(), smsUri.getQueryParams().get("body"));
    } catch (URISyntaxException e) {
      Log.w(TAG, "unable to parse RFC5724 URI from intent", e);
      return new DestinationAndBody("", "");
    }
  }

  private @NonNull DestinationAndBody getDestinationForSyncAdapter(Intent intent) {
    Cursor cursor = null;

    try {
      cursor = getContentResolver().query(intent.getData(), null, null, null, null);

      if (cursor != null && cursor.moveToNext()) {
        return new DestinationAndBody(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.Data.DATA1)), "");
      }

      return new DestinationAndBody("", "");
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  private static class DestinationAndBody {
    private final String destination;
    private final String body;

    private DestinationAndBody(String destination, String body) {
      this.destination = destination;
      this.body = body;
    }

    public String getDestination() {
      return destination;
    }

    public String getBody() {
      return body;
    }
  }
}
