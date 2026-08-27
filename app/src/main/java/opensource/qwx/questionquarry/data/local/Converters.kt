package opensource.qwx.questionquarry.data.local

import androidx.room.TypeConverter
import opensource.qwx.questionquarry.data.local.entity.BlockType
import opensource.qwx.questionquarry.data.local.entity.SessionStatus

class Converters {
    @TypeConverter
    fun fromBlockType(value: BlockType): String = value.name

    @TypeConverter
    fun toBlockType(value: String): BlockType = BlockType.valueOf(value)

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus): String = value.name

    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus = SessionStatus.valueOf(value)
}
