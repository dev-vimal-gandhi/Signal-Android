package com.servalabs.chat.stories.settings.custom

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.DistributionListId
import com.servalabs.chat.database.model.DistributionListRecord
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.sms.MessageSender
import com.servalabs.chat.stories.Stories

class PrivateStorySettingsRepository {
  fun getRecord(distributionListId: DistributionListId): Single<DistributionListRecord> {
    return Single.fromCallable {
      SignalDatabase.distributionLists.getList(distributionListId) ?: error("Record does not exist.")
    }.subscribeOn(Schedulers.io())
  }

  fun removeMember(distributionListRecord: DistributionListRecord, member: RecipientId): Completable {
    return Completable.fromAction {
      SignalDatabase.distributionLists.removeMemberFromList(distributionListRecord.id, distributionListRecord.privacyMode, member)
      Stories.onStorySettingsChanged(distributionListRecord.id)
    }.subscribeOn(Schedulers.io())
  }

  fun delete(distributionListId: DistributionListId): Completable {
    return Completable.fromAction {
      SignalDatabase.distributionLists.deleteList(distributionListId)
      Stories.onStorySettingsChanged(distributionListId)

      val recipientId = SignalDatabase.recipients.getOrInsertFromDistributionListId(distributionListId)
      SignalDatabase.messages.getAllStoriesFor(recipientId, -1).use { reader ->
        for (record in reader) {
          MessageSender.sendRemoteDelete(record.id)
        }
      }
    }.subscribeOn(Schedulers.io())
  }

  fun getRepliesAndReactionsEnabled(distributionListId: DistributionListId): Single<Boolean> {
    return Single.fromCallable {
      SignalDatabase.distributionLists.getStoryType(distributionListId).isStoryWithReplies
    }.subscribeOn(Schedulers.io())
  }

  fun setRepliesAndReactionsEnabled(distributionListId: DistributionListId, repliesAndReactionsEnabled: Boolean): Completable {
    return Completable.fromAction {
      SignalDatabase.distributionLists.setAllowsReplies(distributionListId, repliesAndReactionsEnabled)
      Stories.onStorySettingsChanged(distributionListId)
    }.subscribeOn(Schedulers.io())
  }
}
