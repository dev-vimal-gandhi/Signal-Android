package com.servalabs.chat.payments.preferences;

import kotlin.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import java.util.stream.Collectors;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.database.PaymentTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.payments.Direction;
import com.servalabs.chat.payments.MobileCoinLedgerWrapper;
import com.servalabs.chat.payments.Payment;
import com.servalabs.chat.payments.reconciliation.LedgerReconcile;
import com.servalabs.chat.util.livedata.LiveDataUtil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * General repository for accessing payment information.
 */
public class PaymentsRepository {

  private static final String TAG = Log.tag(PaymentsRepository.class);

  private final PaymentTable            paymentDatabase;
  private final LiveData<List<Payment>> recentPayments;
  private final LiveData<List<Payment>> recentSentPayments;
  private final LiveData<List<Payment>> recentReceivedPayments;

  public PaymentsRepository() {
    paymentDatabase = SignalDatabase.payments();

    LiveData<List<PaymentTable.PaymentTransaction>> localPayments = paymentDatabase.getAllLive();
    LiveData<MobileCoinLedgerWrapper>               ledger        = SignalStore.payments().liveMobileCoinLedger();

    //noinspection NullableProblems
    this.recentPayments         = LiveDataUtil.mapAsync(LiveDataUtil.combineLatest(localPayments, ledger, (a, b) -> new Pair<>(a, b)), p -> reconcile(p.getFirst(), p.getSecond()));
    this.recentSentPayments     = LiveDataUtil.mapAsync(this.recentPayments, p -> filterPayments(p, Direction.SENT));
    this.recentReceivedPayments = LiveDataUtil.mapAsync(this.recentPayments, p -> filterPayments(p, Direction.RECEIVED));
  }

  @WorkerThread
  private @NonNull List<Payment> reconcile(@NonNull Collection<PaymentTable.PaymentTransaction> paymentTransactions, @NonNull MobileCoinLedgerWrapper ledger) {
    List<Payment> reconcile = LedgerReconcile.reconcile(paymentTransactions, ledger);

    updateDatabaseWithNewBlockInformation(reconcile);

    return reconcile;
  }

  private void updateDatabaseWithNewBlockInformation(@NonNull List<Payment> reconcileOutput) {
    List<LedgerReconcile.BlockOverridePayment> blockOverridePayments = reconcileOutput.stream()
                                                                                      .filter(x -> x instanceof LedgerReconcile.BlockOverridePayment)
                                                                                      .map(x -> (LedgerReconcile.BlockOverridePayment)x).collect(Collectors.toList());

    if (blockOverridePayments.isEmpty()) {
      return;
    }
    Log.i(TAG, String.format(Locale.US, "%d payments have new block index or timestamp information", blockOverridePayments.size()));

    for (LedgerReconcile.BlockOverridePayment blockOverridePayment : blockOverridePayments) {
      Payment inner    = blockOverridePayment.getInner();
      boolean override = false;
      if (inner.getBlockIndex() != blockOverridePayment.getBlockIndex()) {
        override = true;
      }
      if (inner.getBlockTimestamp() != blockOverridePayment.getBlockTimestamp()) {
        override = true;
      }
      if (!override) {
        Log.w(TAG, "  Unnecessary");
      } else {
        if (paymentDatabase.updateBlockDetails(inner.getUuid(), blockOverridePayment.getBlockIndex(), blockOverridePayment.getBlockTimestamp())) {
          Log.d(TAG, "  Updated block details for " + inner.getUuid());
        } else {
          Log.w(TAG, "  Failed to update block details for " + inner.getUuid());
        }
      }
    }
  }

  public @NonNull LiveData<List<Payment>> getRecentPayments() {
    return recentPayments;
  }

  public @NonNull LiveData<List<Payment>> getRecentSentPayments() {
    return recentSentPayments;
  }

  public @NonNull LiveData<List<Payment>> getRecentReceivedPayments() {
    return recentReceivedPayments;
  }

  private @NonNull List<Payment> filterPayments(@NonNull List<Payment> payments,
                                                @NonNull Direction direction)
  {
    return payments.stream()
                   .filter(p -> p.getDirection() == direction).collect(Collectors.toList());
  }
}
