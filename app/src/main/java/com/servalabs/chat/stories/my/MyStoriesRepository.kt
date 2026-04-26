package com.servalabs.chat.stories.my

import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import com.servalabs.chat.conversation.ConversationMessage
import com.servalabs.chat.database.GroupReceiptTable
import com.servalabs.chat.database.RxDatabaseObserver
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.MessageRecord
import com.servalabs.chat.database.withAttachments
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.sms.MessageSender

class MyStoriesRepository(context: Context) {

  private val context = context.applicationContext

  fun resend(story: MessageRecord): Completable {
    return Completable.fromAction {
      MessageSender.resend(context, story)
    }.subscribeOn(Schedulers.io())
  }

  fun getMyStories(): Observable<List<MyStoriesState.DistributionSet>> {
    return RxDatabaseObserver
      .conversationList
      .toObservable()
      .map {
        val allRecords = mutableListOf<MessageRecord>()
        SignalDatabase.messages.getAllOutgoingStories(true, -1).use {
          for (messageRecord in it) {
            allRecords.add(messageRecord)
          }
        }

        val withAttachments = allRecords.withAttachments()
        val storiesMap = mutableMapOf<Recipient, List<MessageRecord>>()
        for (record in withAttachments) {
          val currentList = storiesMap[record.toRecipient] ?: emptyList()
          storiesMap[record.toRecipient] = (currentList + record)
        }

        storiesMap.toSortedMap(MyStoryBiasComparator()).map { (r, m) -> createDistributionSet(r, m) }
      }
  }

  private fun createDistributionSet(recipient: Recipient, messageRecords: List<MessageRecord>): MyStoriesState.DistributionSet {
    return MyStoriesState.DistributionSet(
      label = recipient.resolve().getDisplayName(context),
      stories = messageRecords.map { record ->
        MyStoriesState.DistributionStory(
          message = ConversationMessage.ConversationMessageFactory.createWithUnresolvedData(context, record, recipient),
          views = SignalDatabase.groupReceipts.getGroupReceiptInfo(record.id).count { it.status == GroupReceiptTable.STATUS_VIEWED }
        )
      }
    )
  }

  /**
   * Biases "My Story" to the top of the list.
   */
  class MyStoryBiasComparator : Comparator<Recipient> {
    override fun compare(o1: Recipient, o2: Recipient): Int {
      return when {
        o1 == o2 -> 0
        o1.isMyStory -> -1
        o2.isMyStory -> 1
        else -> -1
      }
    }
  }
}
