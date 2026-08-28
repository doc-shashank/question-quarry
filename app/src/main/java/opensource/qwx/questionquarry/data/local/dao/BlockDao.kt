package opensource.qwx.questionquarry.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import opensource.qwx.questionquarry.data.local.entity.Block
import opensource.qwx.questionquarry.data.local.entity.Preset
import opensource.qwx.questionquarry.data.local.entity.Session

@Dao
interface BlockDao {
    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<Session>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionByIdSync(sessionId: Long): Session

    @Query("SELECT * FROM sessions WHERE title = :title LIMIT 1")
    suspend fun getSessionByTitleSync(title: String): Session?

    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId ORDER BY pairIndex ASC, isQuestion DESC, orderIndex ASC")
    suspend fun getAllBlocksForSessionSync(sessionId: Long): List<Block>

    @Query("SELECT * FROM sessions WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    fun getSessionsForToday(startOfDay: Long, endOfDay: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE date >= :start AND date <= :end ORDER BY date DESC")
    fun getSessionsByDateRange(start: Long, end: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE nextReviewDate <= :now AND status = 'DONE' ORDER BY nextReviewDate ASC")
    fun getDueSessions(now: Long): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId ORDER BY pairIndex ASC, isQuestion DESC, orderIndex ASC")
    fun getAllBlocksForSession(sessionId: Long): Flow<List<Block>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: Block): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<Block>)

    @Update
    suspend fun updateBlocks(blocks: List<Block>)

    @Query("UPDATE blocks SET sessionId = :newSessionId WHERE sessionId = :oldSessionId")
    suspend fun reparentBlocks(oldSessionId: Long, newSessionId: Long)

    @Update
    suspend fun updateBlock(block: Block)

    @Delete
    suspend fun deleteBlock(block: Block)
    
    @Query("DELETE FROM blocks WHERE sessionId = :sessionId")
    suspend fun deleteBlocksForSession(sessionId: Long)

    @Query("SELECT DISTINCT subject FROM sessions WHERE subject IS NOT NULL AND subject != ''")
    fun getDistinctSubjects(): Flow<List<String>>

    @Query("SELECT DISTINCT chapterNumber FROM sessions WHERE chapterNumber IS NOT NULL AND chapterNumber != ''")
    fun getDistinctChapterNumbers(): Flow<List<String>>

    @Query("SELECT DISTINCT chapterName FROM sessions WHERE chapterName IS NOT NULL AND chapterName != ''")
    fun getDistinctChapterNames(): Flow<List<String>>

    @Query("SELECT DISTINCT topic FROM sessions WHERE topic IS NOT NULL AND topic != ''")
    fun getDistinctTopics(): Flow<List<String>>

    @Query("SELECT * FROM sessions WHERE subject IS NULL OR subject = ''")
    fun getUntaggedSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE subject = :subject AND (chapterName = :chapter OR (chapterName IS NULL AND :chapter IS NULL))")
    fun getSessionsBySubjectAndChapter(subject: String, chapter: String?): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: Preset): Long

    @Update
    suspend fun updatePreset(preset: Preset)

    @Delete
    suspend fun deletePreset(preset: Preset)

    @Query("SELECT * FROM presets WHERE type = 'SUBJECT'")
    fun getAllSubjects(): Flow<List<Preset>>

    @Query("SELECT * FROM presets WHERE type = 'CHAPTER' AND parentId = :subjectId")
    fun getChaptersForSubject(subjectId: Long): Flow<List<Preset>>

    @Query("SELECT * FROM presets WHERE type = 'TOPIC' AND parentId = :chapterId")
    fun getTopicsForChapter(chapterId: Long): Flow<List<Preset>>

    @Query("SELECT DISTINCT name FROM presets WHERE type = 'CHAPTER'")
    fun getPresetChapterNames(): Flow<List<String>>

    @Query("SELECT DISTINCT name FROM presets WHERE type = 'TOPIC'")
    fun getPresetTopics(): Flow<List<String>>

    @Query("SELECT DISTINCT name FROM presets WHERE type = 'CHAPTER' AND subject = :subjectName")
    fun getChaptersBySubjectName(subjectName: String): Flow<List<String>>

    @Query("SELECT DISTINCT chapterName FROM sessions WHERE subject = :subjectName AND chapterName IS NOT NULL AND chapterName != ''")
    fun getSessionChaptersBySubjectName(subjectName: String): Flow<List<String>>
}
