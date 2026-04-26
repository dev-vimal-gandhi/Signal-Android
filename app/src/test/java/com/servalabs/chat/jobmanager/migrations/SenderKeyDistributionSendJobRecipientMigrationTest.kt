package com.servalabs.chat.jobmanager.migrations

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.signal.core.util.Util
import com.servalabs.chat.database.GroupTable
import com.servalabs.chat.database.model.GroupRecord
import com.servalabs.chat.groups.GroupId
import com.servalabs.chat.jobmanager.JobMigration.JobData
import com.servalabs.chat.jobmanager.JsonJobData
import com.servalabs.chat.jobs.FailingJob
import com.servalabs.chat.jobs.SenderKeyDistributionSendJob
import com.servalabs.chat.recipients.RecipientId
import java.util.Optional

class SenderKeyDistributionSendJobRecipientMigrationTest {
  private val mockDatabase = mockk<GroupTable>(relaxed = true)
  private val testSubject = SenderKeyDistributionSendJobRecipientMigration(mockDatabase)

  @Test
  fun normalMigration() {
    // GIVEN
    val jobData = JobData(
      factoryKey = SenderKeyDistributionSendJob.KEY,
      queueKey = "asdf",
      maxAttempts = -1,
      lifespan = -1,
      data = JsonJobData.Builder()
        .putString("recipient_id", RecipientId.from(1).serialize())
        .putBlobAsString("group_id", GROUP_ID.decodedId)
        .serialize()
    )

    val mockGroup = mockk<GroupRecord> {
      every { recipientId } returns RecipientId.from(2)
    }
    every { mockDatabase.getGroup(GROUP_ID) } returns Optional.of(mockGroup)

    // WHEN
    val result = testSubject.migrate(jobData)
    val data = JsonJobData.deserialize(result.data)

    // THEN
    assertEquals(RecipientId.from(1).serialize(), data.getString("recipient_id"))
    assertEquals(RecipientId.from(2).serialize(), data.getString("thread_recipient_id"))
  }

  @Test
  fun cannotFindGroup() {
    // GIVEN
    val jobData = JobData(
      factoryKey = SenderKeyDistributionSendJob.KEY,
      queueKey = "asdf",
      maxAttempts = -1,
      lifespan = -1,
      data = JsonJobData.Builder()
        .putString("recipient_id", RecipientId.from(1).serialize())
        .putBlobAsString("group_id", GROUP_ID.decodedId)
        .serialize()
    )

    // WHEN
    val result = testSubject.migrate(jobData)

    // THEN
    assertEquals(FailingJob.KEY, result.factoryKey)
  }

  @Test
  fun missingGroupId() {
    // GIVEN
    val jobData = JobData(
      factoryKey = SenderKeyDistributionSendJob.KEY,
      queueKey = "asdf",
      maxAttempts = -1,
      lifespan = -1,
      data = JsonJobData.Builder()
        .putString("recipient_id", RecipientId.from(1).serialize())
        .serialize()
    )

    // WHEN
    val result = testSubject.migrate(jobData)

    // THEN
    assertEquals(FailingJob.KEY, result.factoryKey)
  }

  companion object {
    private val GROUP_ID: GroupId = GroupId.pushOrThrow(Util.getSecretBytes(32))
  }
}
