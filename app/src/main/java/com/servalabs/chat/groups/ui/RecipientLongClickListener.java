package com.servalabs.chat.groups.ui;

import androidx.annotation.NonNull;

import com.servalabs.chat.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
