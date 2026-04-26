package com.servalabs.chat.groups.ui;

import androidx.annotation.NonNull;

import com.servalabs.chat.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
