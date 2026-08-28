package opensource.qwx.questionquarry.ui.test

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import opensource.qwx.questionquarry.data.local.dao.BlockDao
import opensource.qwx.questionquarry.data.local.entity.Block

data class TestFlashcard(
    val sessionId: Long,
    val pairIndex: Int,
    val questionContent: String,
    val answerContent: String,
)

class TestViewModel(private val blockDao: BlockDao) : ViewModel() {

    var flashcards by mutableStateOf<List<TestFlashcard>>(emptyList())
        private set

    var currentIndex by mutableIntStateOf(value = 0)
        private set

    var isFlipped by mutableStateOf(value = false)
        private set

    var secondsElapsed by mutableLongStateOf(0L)
        private set

    var isTestComplete by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(true)
        private set

    private var stopwatchJob: Job? = null

    fun loadSessions(sessionIds: List<Long>) {
        viewModelScope.launch {
            isLoading = true
            val allBlocks = mutableListOf<Block>()
            sessionIds.forEach { sessionId ->
                allBlocks.addAll(blockDao.getAllBlocksForSessionSync(sessionId))
            }

            val grouped = allBlocks.groupBy { "${it.sessionId}_${it.pairIndex}" }
            val cards = grouped.values.map { blocks ->
                val question = blocks.filter { it.isQuestion }
                    .sortedBy { it.orderIndex }
                    .joinToString("\n\n") { block ->
                        if (block.type == opensource.qwx.questionquarry.data.local.entity.BlockType.IMAGE) {
                            "![image](${block.content})"
                        } else {
                            block.content
                        }
                    }
                
                val answer = blocks.filter { !it.isQuestion }
                    .sortedBy { it.orderIndex }
                    .joinToString("\n\n") { block ->
                        if (block.type == opensource.qwx.questionquarry.data.local.entity.BlockType.IMAGE) {
                            "![image](${block.content})"
                        } else {
                            block.content
                        }
                    }

                TestFlashcard(
                    sessionId = blocks.first().sessionId,
                    pairIndex = blocks.first().pairIndex,
                    questionContent = question,
                    answerContent = answer
                )
            }.filter { it.questionContent.isNotBlank() || it.answerContent.isNotBlank() }.shuffled()

            flashcards = cards
            currentIndex = 0
            isFlipped = false
            isTestComplete = cards.isEmpty()
            isLoading = false
            if (cards.isNotEmpty()) {
                startStopwatch()
            }
        }
    }

    private fun startStopwatch() {
        stopwatchJob?.cancel()
        secondsElapsed = 0
        stopwatchJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                secondsElapsed++
            }
        }
    }

    fun flip() {
        isFlipped = !isFlipped
    }

    fun next() {
        if (currentIndex < (flashcards.size - 1)) {
            currentIndex++
            isFlipped = false
        } else {
            isTestComplete = true
            stopwatchJob?.cancel()
        }
    }

    fun formatTime(): String {
        val minutes = secondsElapsed / 60
        val seconds = secondsElapsed % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    override fun onCleared() {
        stopwatchJob?.cancel()
    }
}
