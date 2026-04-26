/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.jobmanager.impl

import android.app.job.JobInfo
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.Constraint
import com.servalabs.chat.jobmanager.ConstraintObserver
import com.servalabs.chat.jobs.StickerDownloadJob
import com.servalabs.chat.jobs.StickerPackDownloadJob

/**
 * When met, no sticker download jobs should be in the job queue/running.
 */
object StickersNotDownloadingConstraint : Constraint {

  const val KEY = "StickersNotDownloadingConstraint"

  private val factoryKeys = setOf(StickerPackDownloadJob.KEY, StickerDownloadJob.KEY)

  override fun isMet(): Boolean {
    return AppDependencies.jobManager.areFactoriesEmpty(factoryKeys)
  }

  override fun getFactoryKey(): String = KEY

  override fun applyToJobInfo(jobInfoBuilder: JobInfo.Builder) = Unit

  object Observer : ConstraintObserver {
    override fun register(notifier: ConstraintObserver.Notifier) {
      AppDependencies.jobManager.addListener({ job -> factoryKeys.contains(job.factoryKey) }) { job, jobState ->
        if (jobState.isComplete) {
          if (isMet) {
            notifier.onConstraintMet(KEY)
          }
        }
      }
    }
  }

  class Factory : Constraint.Factory<StickersNotDownloadingConstraint> {
    override fun create(): StickersNotDownloadingConstraint {
      return StickersNotDownloadingConstraint
    }
  }
}
