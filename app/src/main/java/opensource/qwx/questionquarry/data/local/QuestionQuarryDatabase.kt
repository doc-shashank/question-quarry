package opensource.qwx.questionquarry.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import opensource.qwx.questionquarry.data.local.dao.BlockDao
import opensource.qwx.questionquarry.data.local.entity.Block
import opensource.qwx.questionquarry.data.local.entity.Preset
import opensource.qwx.questionquarry.data.local.entity.Session

@Database(entities = [Session::class, Block::class, Preset::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class QuestionQuarryDatabase : RoomDatabase() {
    abstract fun blockDao(): BlockDao
}
