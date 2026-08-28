package opensource.qwx.questionquarry.ui.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class CustomTextToolbar(
    val onCopyRequested: () -> Unit,
    val onCutRequested: () -> Unit,
    val onPasteRequested: () -> Unit,
    val onSelectAllRequested: () -> Unit,
    val onBoldRequested: () -> Unit,
    val onItalicRequested: () -> Unit,
    val onUnderlineRequested: () -> Unit
) : TextToolbar {
    
    private var _status by mutableStateOf(TextToolbarStatus.Hidden)
    override val status: TextToolbarStatus
        get() = _status
        
    var lastRect by mutableStateOf(Rect.Zero)
        private set

    override fun hide() {
        _status = TextToolbarStatus.Hidden
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        lastRect = rect
        _status = TextToolbarStatus.Shown
    }
}
