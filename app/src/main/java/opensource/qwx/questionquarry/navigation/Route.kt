package opensource.qwx.questionquarry.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data class Home(
        val filterDate: Long? = null
    ) : Route

    @Serializable
    data class SessionDetail(val sessionId: Long) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data class DailySessionList(val date: Long) : Route

    @Serializable
    data class NewSession(val title: String, val sessionId: Long? = null) : Route

    @Serializable
    data class TextEditor(
        val pairIndex: Int,
        val blockIndex: Int,
        val isQuestion: Boolean
    ) : Route

    @Serializable
    data class Test(val sessionIds: List<Long>) : Route

    @Serializable
    data object SubjectsBrowser : Route
}
