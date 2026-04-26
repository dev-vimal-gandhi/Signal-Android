package com.servalabs.chat.util

import android.content.Context
import android.content.pm.PackageManager
import org.signal.core.util.logging.Log
import com.servalabs.chat.BuildConfig
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobs.RefreshAttributesJob
import com.servalabs.chat.jobs.RemoteConfigRefreshJob
import com.servalabs.chat.jobs.RetrieveRemoteAnnouncementsJob
import com.servalabs.chat.keyvalue.SignalStore
import java.time.Duration

object VersionTracker {
  private val TAG = Log.tag(VersionTracker::class.java)

  @JvmStatic
  fun getLastSeenVersion(context: Context): Int {
    return TextSecurePreferences.getLastVersionCode(context)
  }

  @JvmStatic
  fun updateLastSeenVersion(context: Context) {
    val currentVersionCode = BuildConfig.VERSION_CODE
    val lastVersionCode = TextSecurePreferences.getLastVersionCode(context)

    if (currentVersionCode != lastVersionCode) {
      Log.i(TAG, "Upgraded from $lastVersionCode to $currentVersionCode. Clearing client deprecation.", true)
      SignalStore.misc.isClientDeprecated = false
      SignalStore.remoteConfig.eTag = ""
      val jobChain = listOf(RemoteConfigRefreshJob(), RefreshAttributesJob())
      AppDependencies.jobManager.startChain(jobChain).enqueue()
      RetrieveRemoteAnnouncementsJob.enqueue(true)
      LocalMetrics.getInstance().clear()
    }

    TextSecurePreferences.setLastVersionCode(context, currentVersionCode)
  }

  @JvmStatic
  fun getDaysSinceFirstInstalled(context: Context): Long {
    return try {
      val installTimestamp = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
      Duration.ofMillis(System.currentTimeMillis() - installTimestamp).toDays()
    } catch (e: PackageManager.NameNotFoundException) {
      Log.w(TAG, e)
      0
    }
  }
}
