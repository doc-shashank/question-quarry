package opensource.qwx.questionquarry.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ColorSchemeId {
    DEFAULT, GREEN, RED, PURPLE
}

class ThemeViewModel(private val context: Context) : ViewModel() {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val colorSchemeKey = stringPreferencesKey("color_scheme_id")

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[themeKey] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(themeName)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val colorSchemeId: Flow<ColorSchemeId> = context.dataStore.data
        .map { preferences ->
            val schemeName = preferences[colorSchemeKey] ?: ColorSchemeId.DEFAULT.name
            ColorSchemeId.valueOf(schemeName)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ColorSchemeId.DEFAULT
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[themeKey] = mode.name
            }
        }
    }

    fun setColorScheme(schemeId: ColorSchemeId) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[colorSchemeKey] = schemeId.name
            }
        }
    }
}
