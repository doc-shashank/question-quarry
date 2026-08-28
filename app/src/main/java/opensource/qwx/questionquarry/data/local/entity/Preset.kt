package opensource.qwx.questionquarry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PresetType {
    SUBJECT, CHAPTER, TOPIC
}

@Entity(tableName = "presets")
data class Preset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null, // parent subject id for chapters, parent chapter id for topics
    val type: PresetType,
    val subject: String? = null, // Denormalized for easier filtering if needed
    val chapter: String? = null,
)
