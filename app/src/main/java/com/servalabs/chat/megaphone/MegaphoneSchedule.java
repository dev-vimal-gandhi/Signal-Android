package com.servalabs.chat.megaphone;

import androidx.annotation.WorkerThread;

public interface MegaphoneSchedule {
  @WorkerThread
  boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime);
}
