package com.servalabs.chat.payments.backup;

import androidx.annotation.NonNull;

import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.payments.Mnemonic;

public final class PaymentsRecoveryRepository {
  public @NonNull Mnemonic getMnemonic() {
    return SignalStore.payments().getPaymentsMnemonic();
  }
}
