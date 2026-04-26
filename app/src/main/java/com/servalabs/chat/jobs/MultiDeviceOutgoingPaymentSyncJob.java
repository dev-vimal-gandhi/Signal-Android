package com.servalabs.chat.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.database.PaymentTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.JsonJobData;
import com.servalabs.chat.jobmanager.impl.NetworkConstraint;
import com.servalabs.chat.jobmanager.impl.SealedSenderConstraint;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.net.NotPushRegisteredException;
import com.servalabs.chat.payments.proto.PaymentMetaData;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.recipients.RecipientUtil;
import org.whispersystems.signalservice.api.messages.multidevice.OutgoingPaymentMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.signal.core.models.ServiceId;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.push.exceptions.ServerRejectedException;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okio.ByteString;

/**
 * Tells a linked device about sent payments.
 */
public final class MultiDeviceOutgoingPaymentSyncJob extends BaseJob {

  private static final String TAG = Log.tag(MultiDeviceOutgoingPaymentSyncJob.class);

  public static final String KEY = "MultiDeviceOutgoingPaymentSyncJob";

  private static final String KEY_UUID = "uuid";

  private final UUID uuid;

  public MultiDeviceOutgoingPaymentSyncJob(@NonNull UUID sentPaymentId) {
    this(new Parameters.Builder()
                       .setQueue("MultiDeviceOutgoingPaymentSyncJob")
                       .addConstraint(NetworkConstraint.KEY)
                       .addConstraint(SealedSenderConstraint.KEY)
                       .setLifespan(TimeUnit.DAYS.toMillis(1))
                       .build(),
         sentPaymentId);
  }

  private MultiDeviceOutgoingPaymentSyncJob(@NonNull Parameters parameters,
                                            @NonNull UUID sentPaymentId)
  {
    super(parameters);
    this.uuid = sentPaymentId;
  }

  @Override
  public @Nullable byte[] serialize() {
    return new JsonJobData.Builder()
                   .putString(KEY_UUID, uuid.toString())
                   .serialize();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  protected void onRun() throws Exception {
    if (!Recipient.self().isRegistered()) {
      throw new NotPushRegisteredException();
    }

    if (!SignalStore.account().isMultiDevice()) {
      Log.i(TAG, "Not multi device, aborting...");
      return;
    }

    PaymentTable.PaymentTransaction payment = SignalDatabase.payments().getPayment(uuid);

    if (payment == null) {
      Log.w(TAG, "Payment not found " + uuid);
      return;
    }

    PaymentMetaData.MobileCoinTxoIdentification txoIdentification = payment.getPaymentMetaData().mobileCoinTxoIdentification;

    boolean defrag = payment.isDefrag();

    Optional<ServiceId> uuid;
    if (!defrag && payment.getPayee().hasRecipientId()) {
      uuid = Optional.of(RecipientUtil.getOrFetchServiceId(context, Recipient.resolved(payment.getPayee().requireRecipientId())));
    } else {
      uuid = Optional.empty();
    }

    byte[] receipt = payment.getReceipt();

    if (receipt == null) {
      throw new AssertionError("Trying to sync payment before sent?");
    }

    OutgoingPaymentMessage outgoingPaymentMessage = new OutgoingPaymentMessage(uuid,
                                                                               payment.getAmount().requireMobileCoin(),
                                                                               payment.getFee().requireMobileCoin(),
                                                                               ByteString.of(receipt),
                                                                               payment.getBlockIndex(),
                                                                               payment.getTimestamp(),
                                                                               defrag ? Optional.empty() : Optional.of(payment.getPayee().requirePublicAddress().serialize()),
                                                                               defrag ? Optional.empty() : Optional.of(payment.getNote()),
                                                                               txoIdentification.publicKey,
                                                                               txoIdentification.keyImages);


    AppDependencies.getSignalServiceMessageSender()
                   .sendSyncMessage(SignalServiceSyncMessage.forOutgoingPayment(outgoingPaymentMessage)
                   );
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    if (e instanceof ServerRejectedException) return false;
    return e instanceof PushNetworkException;
  }

  @Override
  public void onFailure() {
    Log.w(TAG, "Failed to sync sent payment!");
  }

  public static class Factory implements Job.Factory<MultiDeviceOutgoingPaymentSyncJob> {

    @Override
    public @NonNull MultiDeviceOutgoingPaymentSyncJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      JsonJobData data = JsonJobData.deserialize(serializedData);

      return new MultiDeviceOutgoingPaymentSyncJob(parameters,
                                                   UUID.fromString(data.getString(KEY_UUID)));
    }
  }
}
