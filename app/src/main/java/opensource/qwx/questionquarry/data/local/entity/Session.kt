package opensource.qwx.questionquarry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionStatus {
    DRAFT, DONE, REVIEW
}

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val title: String,
    val status: SessionStatus = SessionStatus.DONE,
    val nextReviewDate: Long = 0,
    val intervalDays: Int = 1,
    val completionTime: Long = 0,
    val subject: String? = null,
    val chapterNumber: String? = null,
    val chapterName: String? = null,
    val topic: String? = null,
    val isTopicEnabled: Boolean = false
)
