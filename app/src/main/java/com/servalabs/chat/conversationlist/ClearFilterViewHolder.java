package com.servalabs.chat.conversationlist;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.servalabs.chat.R;
import com.servalabs.chat.conversationlist.model.Conversation;
import com.servalabs.chat.conversationlist.model.ConversationReader;

class ClearFilterViewHolder extends RecyclerView.ViewHolder {

  private final View tip;

  ClearFilterViewHolder(@NonNull View itemView, OnClearFilterClickListener listener) {
    super(itemView);
    tip = itemView.findViewById(R.id.clear_filter_tip);
    itemView.findViewById(R.id.clear_filter).setOnClickListener(v -> {
      listener.onClearFilterClick();
    });
  }

  void bind(@NonNull Conversation conversation) {
    if (conversation.getThreadRecord().getType() == ConversationReader.TYPE_SHOW_TIP) {
      tip.setVisibility(View.VISIBLE);
    } else {
      tip.setVisibility(View.GONE);
    }
  }

  interface OnClearFilterClickListener {
    void onClearFilterClick();
  }
}
