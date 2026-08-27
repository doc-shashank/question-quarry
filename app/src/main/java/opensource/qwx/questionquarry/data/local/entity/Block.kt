package opensource.qwx.questionquarry.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BlockType {
    TEXT, IMAGE
}

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class Block(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val type: BlockType,
    val content: String,
    val orderIndex: Int,
    val isQuestion: Boolean,
    val pairIndex: Int = 0
)
