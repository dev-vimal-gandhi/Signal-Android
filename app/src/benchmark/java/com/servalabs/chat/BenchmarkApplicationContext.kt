/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat

import android.app.Application
import org.signal.libsignal.net.Network
import com.servalabs.chat.database.JobDatabase
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.dependencies.ApplicationDependencyProvider
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.jobmanager.JobManager
import com.servalabs.chat.jobmanager.JobMigrator
import com.servalabs.chat.jobmanager.impl.FactoryJobPredicate
import com.servalabs.chat.jobs.AccountConsistencyWorkerJob
import com.servalabs.chat.jobs.ArchiveBackupIdReservationJob
import com.servalabs.chat.jobs.AttachmentCompressionJob
import com.servalabs.chat.jobs.AttachmentUploadJob
import com.servalabs.chat.jobs.AvatarGroupsV2DownloadJob
import com.servalabs.chat.jobs.CreateReleaseChannelJob
import com.servalabs.chat.jobs.DirectoryRefreshJob
import com.servalabs.chat.jobs.DownloadLatestEmojiDataJob
import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob
import com.servalabs.chat.jobs.FastJobStorage
import com.servalabs.chat.jobs.FontDownloaderJob
import com.servalabs.chat.jobs.GroupCallUpdateSendJob
import com.servalabs.chat.jobs.GroupRingCleanupJob
import com.servalabs.chat.jobs.GroupV2UpdateSelfProfileKeyJob
import com.servalabs.chat.jobs.IndividualSendJob
import com.servalabs.chat.jobs.JobManagerFactories
import com.servalabs.chat.jobs.LinkedDeviceInactiveCheckJob
import com.servalabs.chat.jobs.MarkerJob
import com.servalabs.chat.jobs.MultiDeviceProfileKeyUpdateJob
import com.servalabs.chat.jobs.PostRegistrationBackupRedemptionJob
import com.servalabs.chat.jobs.PreKeysSyncJob
import com.servalabs.chat.jobs.ProfileUploadJob
import com.servalabs.chat.jobs.PushGroupSendJob
import com.servalabs.chat.jobs.PushProcessMessageJob
import com.servalabs.chat.jobs.ReactionSendJob
import com.servalabs.chat.jobs.RefreshAttributesJob
import com.servalabs.chat.jobs.RefreshSvrCredentialsJob
import com.servalabs.chat.jobs.RequestGroupV2InfoJob
import com.servalabs.chat.jobs.ResetSvrGuessCountJob
import com.servalabs.chat.jobs.RestoreOptimizedMediaJob
import com.servalabs.chat.jobs.RetrieveProfileAvatarJob
import com.servalabs.chat.jobs.RetrieveProfileJob
import com.servalabs.chat.jobs.RetrieveRemoteAnnouncementsJob
import com.servalabs.chat.jobs.RotateCertificateJob
import com.servalabs.chat.jobs.SendDeliveryReceiptJob
import com.servalabs.chat.jobs.StickerPackDownloadJob
import com.servalabs.chat.jobs.StorageSyncJob
import com.servalabs.chat.jobs.StoryOnboardingDownloadJob
import com.servalabs.chat.jobs.TypingSendJob
import com.servalabs.chat.net.DeviceTransferBlockingInterceptor
import com.servalabs.chat.util.TextSecurePreferences
import org.whispersystems.signalservice.api.util.UptimeSleepTimer
import org.whispersystems.signalservice.api.websocket.SignalWebSocket
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration
import org.whispersystems.signalservice.internal.websocket.BenchmarkWebSocketConnection
import java.util.function.Supplier
import kotlin.time.Duration.Companion.seconds

class BenchmarkApplicationContext : ApplicationContext() {

  override fun initializeAppDependencies() {
    AppDependencies.init(this, BenchmarkDependencyProvider(this, ApplicationDependencyProvider(this)))

    DeviceTransferBlockingInterceptor.getInstance().blockNetwork()
  }

  override fun onForeground() = Unit

  class BenchmarkDependencyProvider(val application: Application, private val default: ApplicationDependencyProvider) : AppDependencies.Provider by default {
    override fun provideAuthWebSocket(
      signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>,
      libSignalNetworkSupplier: Supplier<Network>
    ): SignalWebSocket.AuthenticatedWebSocket {
      return SignalWebSocket.AuthenticatedWebSocket(
        connectionFactory = { BenchmarkWebSocketConnection.createAuthInstance() },
        canConnect = { true },
        sleepTimer = UptimeSleepTimer(),
        disconnectTimeoutMs = 15.seconds.inWholeMilliseconds
      )
    }

    override fun provideUnauthWebSocket(
      signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>,
      libSignalNetworkSupplier: Supplier<Network>
    ): SignalWebSocket.UnauthenticatedWebSocket {
      return SignalWebSocket.UnauthenticatedWebSocket(
        connectionFactory = { BenchmarkWebSocketConnection.createUnauthInstance() },
        canConnect = { true },
        sleepTimer = UptimeSleepTimer(),
        disconnectTimeoutMs = 15.seconds.inWholeMilliseconds
      )
    }

    override fun provideJobManager(): JobManager {
      val config = JobManager.Configuration.Builder()
        .setJobFactories(filterJobFactories(JobManagerFactories.getJobFactories(application)))
        .setConstraintFactories(JobManagerFactories.getConstraintFactories(application))
        .setConstraintObservers(JobManagerFactories.getConstraintObservers(application))
        .setJobStorage(FastJobStorage(JobDatabase.getInstance(application)))
        .setJobMigrator(JobMigrator(TextSecurePreferences.getJobManagerVersion(application), JobManager.CURRENT_VERSION, JobManagerFactories.getJobMigrations(application)))
        .addReservedJobRunner(FactoryJobPredicate(PushProcessMessageJob.KEY, MarkerJob.KEY))
        .addReservedJobRunner(FactoryJobPredicate(AttachmentUploadJob.KEY, AttachmentCompressionJob.KEY))
        .addReservedJobRunner(
          FactoryJobPredicate(
            IndividualSendJob.KEY,
            PushGroupSendJob.KEY,
            ReactionSendJob.KEY,
            TypingSendJob.KEY,
            GroupCallUpdateSendJob.KEY,
            SendDeliveryReceiptJob.KEY
          )
        )
        .build()
      return JobManager(application, config)
    }

    private fun filterJobFactories(jobFactories: Map<String, Job.Factory<*>>): Map<String, Job.Factory<*>> {
      val blockedJobs = setOf(
        AccountConsistencyWorkerJob.KEY,
        ArchiveBackupIdReservationJob.KEY,
        AvatarGroupsV2DownloadJob.KEY,
        CreateReleaseChannelJob.KEY,
        DirectoryRefreshJob.KEY,
        DownloadLatestEmojiDataJob.KEY,
        EmojiSearchIndexDownloadJob.KEY,
        FontDownloaderJob.KEY,
        GroupRingCleanupJob.KEY,
        GroupV2UpdateSelfProfileKeyJob.KEY,
        LinkedDeviceInactiveCheckJob.KEY,
        MultiDeviceProfileKeyUpdateJob.KEY,
        PostRegistrationBackupRedemptionJob.KEY,
        PreKeysSyncJob.KEY,
        ProfileUploadJob.KEY,
        RefreshAttributesJob.KEY,
        RefreshSvrCredentialsJob.KEY,
        RequestGroupV2InfoJob.KEY,
        ResetSvrGuessCountJob.KEY,
        RestoreOptimizedMediaJob.KEY,
        RetrieveProfileAvatarJob.KEY,
        RetrieveProfileJob.KEY,
        RetrieveRemoteAnnouncementsJob.KEY,
        RotateCertificateJob.KEY,
        StickerPackDownloadJob.KEY,
        StorageSyncJob.KEY,
        StoryOnboardingDownloadJob.KEY
      )

      return jobFactories.mapValues {
        if (it.key in blockedJobs) {
          NoOpJob.Factory()
        } else {
          it.value
        }
      }
    }
  }

  private class NoOpJob(parameters: Parameters) : Job(parameters) {

    companion object {
      const val KEY = "NoOpJob"
    }

    override fun serialize(): ByteArray? = null
    override fun getFactoryKey(): String = KEY
    override fun run(): Result = Result.success()
    override fun onFailure() = Unit

    class Factory : Job.Factory<NoOpJob> {
      override fun create(parameters: Parameters, serializedData: ByteArray?): NoOpJob {
        return NoOpJob(parameters)
      }
    }
  }
}
