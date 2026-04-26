package com.servalabs.chat.jobs;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import org.signal.libsignal.zkgroup.profiles.ProfileKey;
import com.servalabs.chat.BuildConfig;
import com.servalabs.chat.crypto.ProfileKeyUtil;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.impl.NetworkConstraint;
import com.servalabs.chat.jobmanager.impl.SealedSenderConstraint;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.net.NotPushRegisteredException;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.util.RemoteConfig;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.AttachmentCipherStreamUtil;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.internal.crypto.PaddingInputStream;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentStream;
import org.whispersystems.signalservice.api.messages.multidevice.ContactsMessage;
import org.whispersystems.signalservice.api.messages.multidevice.DeviceContact;
import org.whispersystems.signalservice.api.messages.multidevice.DeviceContactsOutputStream;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.push.exceptions.ServerRejectedException;
import org.whispersystems.signalservice.internal.push.http.ResumableUploadSpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MultiDeviceProfileKeyUpdateJob extends BaseJob {

  public static String KEY = "MultiDeviceProfileKeyUpdateJob";

  private static final String TAG = Log.tag(MultiDeviceProfileKeyUpdateJob.class);

  public MultiDeviceProfileKeyUpdateJob() {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .addConstraint(SealedSenderConstraint.KEY)
                           .setQueue("MultiDeviceProfileKeyUpdateJob")
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build());
  }

  private MultiDeviceProfileKeyUpdateJob(@NonNull Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public @Nullable byte[] serialize() {
    return null;
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

    ByteArrayOutputStream      baos = new ByteArrayOutputStream();
    DeviceContactsOutputStream out  = new DeviceContactsOutputStream(baos, RemoteConfig.useBinaryId(), BuildConfig.USE_STRING_ID);

    out.write(new DeviceContact(Optional.ofNullable(SignalStore.account().getAci()),
                                Optional.ofNullable(SignalStore.account().getE164()),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()));

    out.close();

    SignalServiceMessageSender    messageSender    = AppDependencies.getSignalServiceMessageSender();
    long                          dataLength       = baos.toByteArray().length;
    long                          ciphertextLength = AttachmentCipherStreamUtil.getCiphertextLength(PaddingInputStream.getPaddedSize(dataLength));
    ResumableUploadSpec           uploadSpec       = messageSender.getResumableUploadSpec(ciphertextLength);
    SignalServiceAttachmentStream attachmentStream = SignalServiceAttachment.newStreamBuilder()
                                                                            .withStream(new ByteArrayInputStream(baos.toByteArray()))
                                                                            .withContentType("application/octet-stream")
                                                                            .withLength(dataLength)
                                                                            .withResumableUploadSpec(uploadSpec)
                                                                            .build();

    SignalServiceSyncMessage syncMessage = SignalServiceSyncMessage.forContacts(new ContactsMessage(attachmentStream, false));

    messageSender.sendSyncMessage(syncMessage);
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof ServerRejectedException) return false;
    if (exception instanceof PushNetworkException) return true;
    return false;
  }

  @Override
  public void onFailure() {
    Log.w(TAG, "Profile key sync failed!");
  }

  public static final class Factory implements Job.Factory<MultiDeviceProfileKeyUpdateJob> {
    @Override
    public @NonNull MultiDeviceProfileKeyUpdateJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new MultiDeviceProfileKeyUpdateJob(parameters);
    }
  }
}
