/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.util

import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.jobmanager.JobManager

/** Starts a new chain with this job. */
fun Job.asChain(): JobManager.Chain {
  return AppDependencies.jobManager.startChain(this)
}
