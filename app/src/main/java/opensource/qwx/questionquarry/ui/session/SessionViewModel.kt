package opensource.qwx.questionquarry.ui.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import opensource.qwx.questionquarry.data.local.dao.BlockDao
import opensource.qwx.questionquarry.data.local.entity.Block
import opensource.qwx.questionquarry.data.local.entity.BlockType
import opensource.qwx.questionquarry.data.local.entity.Preset
import opensource.qwx.questionquarry.data.local.entity.PresetType
import opensource.qwx.questionquarry.data.local.entity.Session
import opensource.qwx.questionquarry.data.local.entity.SessionStatus
import opensource.qwx.questionquarry.data.cache.SessionCache
import opensource.qwx.questionquarry.ui.components.CanvasBlock
import java.util.*

private const val DUMMY_SESSION_TITLE = "DummySession0"

enum class RecommendationType {
    SUBJECT, CHAPTER_NUMBER, CHAPTER_NAME, TOPIC
}

class SessionViewModel(private val blockDao: BlockDao) : ViewModel() {

    private val sessionCache = SessionCache(10)

    var isLoading by mutableStateOf(value = false)
        private set

    data class QAPair(
        val questionBlocks: List<CanvasBlock>,
        val answerBlocks: List<CanvasBlock>,
    )

    var draftPairs by mutableStateOf(listOf(QAPair(emptyList(), emptyList())))
        private set

    var editingSessionId by mutableStateOf<Long?>(null)
        private set

    var draftSubject by mutableStateOf("")
    var draftChapterNumber by mutableStateOf("")
    var draftChapterName by mutableStateOf("")
    var draftTopic by mutableStateOf("")
    var isTopicEnabled by mutableStateOf(false)

    val allSessionsByDate = blockDao.getAllSessions()
        .map { sessions ->
            sessions.groupBy { session ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = if (session.completionTime > 0) session.completionTime else session.date
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    val subjects = blockDao.getDistinctSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapterNumbers = blockDao.getDistinctChapterNumbers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapterNames = blockDao.getDistinctChapterNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topics = blockDao.getDistinctTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presetSubjects = blockDao.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presetChapterNames = blockDao.getPresetChapterNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presetTopics = blockDao.getPresetTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getRecommendations(query: String, type: RecommendationType): List<String> {
        if (query.isBlank()) return emptyList()
        val list: List<String> = when (type) {
            RecommendationType.SUBJECT -> {
                (subjects.value + presetSubjects.value.map { it.name }).distinct()
            }
            RecommendationType.CHAPTER_NUMBER -> {
                chapterNumbers.value
            }
            RecommendationType.CHAPTER_NAME -> {
                (chapterNames.value + presetChapterNames.value).distinct()
            }
            RecommendationType.TOPIC -> {
                (topics.value + presetTopics.value).distinct()
            }
        }
        return list.asSequence().filter { it.contains(query, ignoreCase = true) }.take(3).toList()
    }

    fun getSessionCountOnDate(dateMillis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return allSessionsByDate.value[cal.timeInMillis]?.size ?: 0
    }

    fun hasSessionsOnDate(dateMillis: Long): Boolean {
        return getSessionCountOnDate(dateMillis) > 0
    }

    fun getDaysOfMonth(calendar: Calendar): List<Calendar?> {
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<Calendar?>()
        repeat(firstDayOfWeek) {
            days.add(null)
        }
        for (i in 1..daysInMonth) {
            val day = monthCalendar.clone() as Calendar
            day.set(Calendar.DAY_OF_MONTH, i)
            days.add(day)
        }
        return days
    }

    fun updateDraftPairs(newPairs: List<QAPair>) {
        draftPairs = newPairs
    }

    fun loadSessionForEditing(sessionId: Long) {
        viewModelScope.launch {
            editingSessionId = sessionId
            val session = blockDao.getSessionByIdSync(sessionId)
            draftSubject = session.subject ?: ""
            draftChapterNumber = session.chapterNumber ?: ""
            draftChapterName = session.chapterName ?: ""
            draftTopic = session.topic ?: ""
            isTopicEnabled = session.isTopicEnabled
            
            val entities = blockDao.getAllBlocksForSession(sessionId).first()
            val pairsMap = entities.groupBy { it.pairIndex }
            val newPairs = pairsMap.keys.sorted().map { pairIndex ->
                val pairBlocks = pairsMap[pairIndex] ?: emptyList()
                val questions = pairBlocks.filter { it.isQuestion }.sortedBy { it.orderIndex }
                val answers = pairBlocks.filter { !it.isQuestion }.sortedBy { it.orderIndex }
                
                QAPair(
                    questionBlocks = questions.map { mapEntityToCanvasBlock(it) },
                    answerBlocks = answers.map { mapEntityToCanvasBlock(it) }
                )
            }
            if (newPairs.isNotEmpty()) {
                draftPairs = newPairs
            }
        }
    }

    private fun mapEntityToCanvasBlock(entity: Block): CanvasBlock {
        return if (entity.type == BlockType.TEXT) {
            CanvasBlock.Text(entity.content, id = entity.id.toString())
        } else {
            CanvasBlock.Image(entity.content, id = entity.id.toString())
        }
    }

    fun updateTextBlock(pairIndex: Int, blockId: String, isQuestion: Boolean, newContent: String) {
        val newPairs = draftPairs.toMutableList()
        val pair = newPairs[pairIndex]
        
        if (isQuestion) {
            val newBlocks = pair.questionBlocks.map { 
                if ((it.id == blockId) && (it is CanvasBlock.Text)) it.copy(content = newContent) else it
            }
            newPairs[pairIndex] = pair.copy(questionBlocks = newBlocks)
        } else {
            val newBlocks = pair.answerBlocks.map { 
                if ((it.id == blockId) && (it is CanvasBlock.Text)) it.copy(content = newContent) else it
            }
            newPairs[pairIndex] = pair.copy(answerBlocks = newBlocks)
        }
        draftPairs = newPairs
    }

    fun deleteTextBlock(pairIndex: Int, blockId: String, isQuestion: Boolean) {
        val newPairs = draftPairs.toMutableList()
        val pair = newPairs[pairIndex]
        
        if (isQuestion) {
            val newBlocks = pair.questionBlocks.filter { it.id != blockId }
            newPairs[pairIndex] = pair.copy(questionBlocks = newBlocks)
        } else {
            val newBlocks = pair.answerBlocks.filter { it.id != blockId }
            newPairs[pairIndex] = pair.copy(answerBlocks = newBlocks)
        }
        draftPairs = newPairs
    }

    fun resetDraft() {
        draftPairs = listOf(QAPair(emptyList(), emptyList()))
        editingSessionId = null
        draftSubject = ""
        draftChapterNumber = ""
        draftChapterName = ""
        draftTopic = ""
        isTopicEnabled = false
    }

    fun getAllSessions(): Flow<List<Session>> = blockDao.getAllSessions()

    fun getSessionsByDateRange(start: Long, end: Long): Flow<List<Session>> {
        return blockDao.getSessionsByDateRange(start, end)
    }

    fun getRecentSessions(): Flow<List<Session>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -3) // Last 3 days
        val startOfRecent = calendar.timeInMillis
        
        val now = Calendar.getInstance().timeInMillis
        return blockDao.getSessionsByDateRange(startOfRecent, now)
    }

    fun deleteSessions(sessionIds: List<Long>) {
        viewModelScope.launch {
            sessionIds.forEach { id ->
                val session = blockDao.getSessionByIdSync(id)
                if (session.title != DUMMY_SESSION_TITLE) {
                    blockDao.deleteSession(session)
                    sessionCache.remove(id)
                }
            }
        }
    }

    fun deleteSessionWithOptions(sessionId: Long, deleteAll: Boolean) {
        viewModelScope.launch {
            if (deleteAll) {
                val session = blockDao.getSessionByIdSync(sessionId)
                blockDao.deleteSession(session)
                sessionCache.remove(sessionId)
            } else {
                // Dissociate and Delete
                var dummy = blockDao.getSessionByTitleSync(DUMMY_SESSION_TITLE)
                if (dummy == null) {
                    val dummyId = blockDao.insertSession(
                        Session(
                            title = DUMMY_SESSION_TITLE,
                            status = SessionStatus.DONE,
                            date = 0 // permanent/hidden
                        )
                    )
                    dummy = blockDao.getSessionByIdSync(dummyId)
                }
                
                // Reparent blocks to dummy
                blockDao.reparentBlocks(sessionId, dummy.id)
                
                // Delete original session
                val session = blockDao.getSessionByIdSync(sessionId)
                blockDao.deleteSession(session)
                sessionCache.remove(sessionId)
            }
        }
    }

    fun getDueSessions(): Flow<List<Session>> {
        return blockDao.getDueSessions(System.currentTimeMillis())
    }

    fun getUntaggedSessions(): Flow<List<Session>> = blockDao.getUntaggedSessions()

    fun getSessionsBySubjectAndChapter(subject: String, chapter: String?): Flow<List<Session>> = 
        blockDao.getSessionsBySubjectAndChapter(subject, chapter)

    fun getUnifiedChapters(subjectName: String): Flow<List<String>> = combine(
        blockDao.getChaptersBySubjectName(subjectName),
        blockDao.getSessionChaptersBySubjectName(subjectName)
    ) { presets, sessions ->
        (presets + sessions).distinct().sorted()
    }

    fun getChaptersForSubject(subjectId: Long): Flow<List<Preset>> = blockDao.getChaptersForSubject(subjectId)
    fun getTopicsForChapter(chapterId: Long): Flow<List<Preset>> = blockDao.getTopicsForChapter(chapterId)

    fun createPreset(name: String, type: PresetType, parentId: Long? = null, subject: String? = null, chapter: String? = null) {
        viewModelScope.launch {
            blockDao.insertPreset(Preset(name = name, type = type, parentId = parentId, subject = subject, chapter = chapter))
        }
    }

    fun renamePreset(preset: Preset, newName: String) {
        viewModelScope.launch {
            blockDao.updatePreset(preset.copy(name = newName))
        }
    }

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            blockDao.deletePreset(preset)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getSessionDetail(sessionId: Long): Flow<Pair<Session, List<QAPair>>> {
        val cached = sessionCache.get(sessionId)
        
        return flow {
            isLoading = true
            // If cached, emit it immediately (simulating fast load)
            // But we still want to fetch fresh data or at least the full Pair from DAO
            // Actually, the cache stores Session objects. 
            // The task says "When loading a session, first check the cache. If not present, fetch from Room and update the cache."
            
            val sessionFlow = cached?.let { flowOf(it) } ?: blockDao.getSessionById(sessionId).onEach { 
                    sessionCache.put(it)
                }
            
            emitAll(
                sessionFlow.flatMapLatest { session ->
                    blockDao.getAllBlocksForSession(sessionId).map { entities ->
                        val pairsMap = entities.groupBy { it.pairIndex }
                        val pairs = pairsMap.keys.sorted().map { pairIndex ->
                            val pairBlocks = pairsMap[pairIndex] ?: emptyList()
                            val questions = pairBlocks.filter { it.isQuestion }.sortedBy { it.orderIndex }
                            val answers = pairBlocks.filter { !it.isQuestion }.sortedBy { it.orderIndex }
                            
                            QAPair(
                                questionBlocks = questions.map { mapEntityToCanvasBlock(it) },
                                answerBlocks = answers.map { mapEntityToCanvasBlock(it) }
                            )
                        }
                        isLoading = false
                        session to pairs
                    }
                }
            )
        }
    }

    fun saveSession(
        title: String,
        pairs: List<QAPair>,
        subject: String? = null,
        chapterNumber: String? = null,
        chapterName: String? = null,
        topic: String? = null,
        isTopicEnabled: Boolean = false,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val sessionId = if (editingSessionId != null) {
                val originalSession = blockDao.getSessionByIdSync(editingSessionId!!)
                val updatedSession = originalSession.copy(
                    title = title.ifBlank { "Untitled Session" },
                    status = SessionStatus.DONE,
                    nextReviewDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                    completionTime = System.currentTimeMillis(),
                    subject = subject,
                    chapterNumber = chapterNumber,
                    chapterName = chapterName,
                    topic = topic,
                    isTopicEnabled = isTopicEnabled
                )
                blockDao.insertSession(updatedSession)
                if (pairs.isNotEmpty()) {
                    blockDao.deleteBlocksForSession(editingSessionId!!)
                }
                editingSessionId!!
            } else {
                val session = Session(
                    title = title.ifBlank { "Untitled Session" },
                    status = SessionStatus.DONE,
                    nextReviewDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000, // +24 hours
                    completionTime = System.currentTimeMillis(),
                    subject = subject,
                    chapterNumber = chapterNumber,
                    chapterName = chapterName,
                    topic = topic,
                    isTopicEnabled = isTopicEnabled
                )
                blockDao.insertSession(session)
            }

            if (pairs.isNotEmpty()) {
                val blocks = mutableListOf<Block>()
                
                pairs.forEachIndexed { pairIndex, pair ->
                    pair.questionBlocks.forEachIndexed { index, canvasBlock ->
                        blocks.add(mapCanvasBlockToEntity(sessionId, canvasBlock, index, true, pairIndex))
                    }
                    
                    pair.answerBlocks.forEachIndexed { index, canvasBlock ->
                        blocks.add(mapCanvasBlockToEntity(sessionId, canvasBlock, index, false, pairIndex))
                    }
                }
                
                blockDao.insertBlocks(blocks)
            }
            
            // Update cache after save
            val savedSession = blockDao.getSessionByIdSync(sessionId)
            sessionCache.put(savedSession)
            
            // Automatic Tag Creation
            if (!subject.isNullOrBlank()) {
                val existingSubjects = presetSubjects.value.map { it.name }
                if (!existingSubjects.contains(subject)) {
                    val subjectId = blockDao.insertPreset(Preset(name = subject, type = PresetType.SUBJECT))
                    
                    if (!chapterName.isNullOrBlank()) {
                        val chapterId = blockDao.insertPreset(Preset(name = chapterName, type = PresetType.CHAPTER, parentId = subjectId, subject = subject))
                        
                        if (isTopicEnabled && !topic.isNullOrBlank()) {
                            blockDao.insertPreset(Preset(name = topic, type = PresetType.TOPIC, parentId = chapterId, subject = subject, chapter = chapterName))
                        }
                    }
                } else {
                    // Subject exists, check chapter
                    val subjectPreset = presetSubjects.value.find { it.name == subject }
                    if (subjectPreset != null && !chapterName.isNullOrBlank()) {
                        val chaptersInSubject = blockDao.getChaptersForSubject(subjectPreset.id).first()
                        val existingChapters = chaptersInSubject.map { it.name }
                        if (!existingChapters.contains(chapterName)) {
                            val chapterId = blockDao.insertPreset(Preset(name = chapterName, type = PresetType.CHAPTER, parentId = subjectPreset.id, subject = subject))
                            
                            if (isTopicEnabled && !topic.isNullOrBlank()) {
                                blockDao.insertPreset(Preset(name = topic, type = PresetType.TOPIC, parentId = chapterId, subject = subject, chapter = chapterName))
                            }
                        } else {
                            // Chapter exists, check topic
                            val chapterPreset = chaptersInSubject.find { it.name == chapterName }
                            if (chapterPreset != null && isTopicEnabled && !topic.isNullOrBlank()) {
                                val topicsInChapter = blockDao.getTopicsForChapter(chapterPreset.id).first()
                                if (!topicsInChapter.map { it.name }.contains(topic)) {
                                    blockDao.insertPreset(Preset(name = topic, type = PresetType.TOPIC, parentId = chapterPreset.id, subject = subject, chapter = chapterName))
                                }
                            }
                        }
                    }
                }
            }
            
            onComplete()
        }
    }

    private fun mapCanvasBlockToEntity(
        sessionId: Long,
        canvasBlock: CanvasBlock,
        orderIndex: Int,
        isQuestion: Boolean,
        pairIndex: Int
    ): Block {
        return when (canvasBlock) {
            is CanvasBlock.Text -> Block(
                sessionId = sessionId,
                type = BlockType.TEXT,
                content = canvasBlock.content,
                orderIndex = orderIndex,
                isQuestion = isQuestion,
                pairIndex = pairIndex
            )
            is CanvasBlock.Image -> Block(
                sessionId = sessionId,
                type = BlockType.IMAGE,
                content = canvasBlock.imagePath ?: "",
                orderIndex = orderIndex,
                isQuestion = isQuestion,
                pairIndex = pairIndex
            )
        }
    }
}
