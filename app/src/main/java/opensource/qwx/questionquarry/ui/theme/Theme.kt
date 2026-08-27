package opensource.qwx.questionquarry.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import opensource.qwx.questionquarry.ui.settings.ThemeMode
import opensource.qwx.questionquarry.ui.settings.ColorSchemeId

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Green Scheme
private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF558B2F),
    tertiary = Color(0xFF00695C)
)
private val GreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFAED581),
    tertiary = Color(0xFF80CBC4)
)

// Red Scheme
private val RedLightColorScheme = lightColorScheme(
    primary = Color(0xFFC62828),
    secondary = Color(0xFFAD1457),
    tertiary = Color(0xFF6A1B9A)
)
private val RedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE57373),
    secondary = Color(0xFFF06292),
    tertiary = Color(0xFFBA68C8)
)

// Default / Blue Scheme
private val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF0277BD),
    tertiary = Color(0xFF00838F)
)
private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFF4FC3F7),
    tertiary = Color(0xFF4DD0E1)
)

@Composable
fun QuestionQuarryTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorSchemeId: ColorSchemeId = ColorSchemeId.DEFAULT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && colorSchemeId == ColorSchemeId.DEFAULT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            when (colorSchemeId) {
                ColorSchemeId.DEFAULT -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
                ColorSchemeId.GREEN -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
                ColorSchemeId.RED -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
                ColorSchemeId.PURPLE -> if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
