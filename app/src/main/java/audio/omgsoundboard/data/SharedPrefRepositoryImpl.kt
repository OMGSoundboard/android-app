package audio.omgsoundboard.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import audio.omgsoundboard.core.utils.Constants.PARTICLES_STATUS
import audio.omgsoundboard.core.utils.Constants.THEME_TYPE
import audio.omgsoundboard.domain.models.BackgroundType
import audio.omgsoundboard.domain.models.UserPreferences
import audio.omgsoundboard.domain.models.WidgetConfiguration
import audio.omgsoundboard.domain.repository.SharedPrefRepository
import audio.omgsoundboard.presentation.theme.ThemeType
import audio.omgsoundboard.presentation.theme.toThemeType
import audio.omgsoundboard.presentation.utils.backgroundTypeKey
import audio.omgsoundboard.presentation.utils.colorTypeKey
import audio.omgsoundboard.presentation.utils.fontColorKey
import audio.omgsoundboard.presentation.utils.fontSizeKey
import audio.omgsoundboard.presentation.utils.imageTypeKey
import audio.omgsoundboard.presentation.utils.soundIdPreferenceKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SharedPrefRepositoryImpl @Inject constructor(
    private val context: Context
) : SharedPrefRepository {

    private val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    override fun putStringPair(key: String, value: String) {
        with (sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }
    override fun getStringPair(key: String, defaultValue: String) : String {
        return sharedPref?.getString(key, defaultValue) ?: defaultValue
    }

    override fun putBooleanPair(key: String, value: Boolean) {
        with (sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }
    override fun getBooleanPair(key: String, defaultValue: Boolean) : Boolean{
        return sharedPref?.getBoolean(key, defaultValue) ?: defaultValue
    }

    override fun putIntPair(key: String, value: Int) {
        with(sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }
    override fun getIntPair(key: String, defaultValue: Int): Int {
        return sharedPref?.getInt(key, defaultValue) ?: defaultValue
    }


    override fun getUserPreferencesAsFlow(): Flow<UserPreferences> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == THEME_TYPE || key == PARTICLES_STATUS){
                val themePref = sharedPref.getString(THEME_TYPE, ThemeType.DARK.name)

                val data = UserPreferences(
                    selectedTheme = toThemeType(themePref ?: ThemeType.DARK.name) ,
                    areParticlesEnabled = sharedPref.getBoolean(PARTICLES_STATUS, false),
                )
                trySend(data)
            }
        }

        sharedPref.registerOnSharedPreferenceChangeListener(listener)

        if (sharedPref.contains(THEME_TYPE) || sharedPref.contains(PARTICLES_STATUS)) {
            val themePref = sharedPref.getString(THEME_TYPE, ThemeType.DARK.name)

            val data = UserPreferences(
                selectedTheme = toThemeType(themePref ?: ThemeType.DARK.name) ,
                areParticlesEnabled = sharedPref.getBoolean(PARTICLES_STATUS, false),
            )
            send(data)
        }

        awaitClose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.buffer(Channel.UNLIMITED)

    override suspend fun getWidgetPreferences(widgetId: Int): WidgetConfiguration {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
        val prefs: Preferences = getAppWidgetState(
            context = context,
            definition = PreferencesGlanceStateDefinition,
            glanceId = glanceId
        )

        val savedSoundId = prefs[soundIdPreferenceKey]
        val savedBackgroundTypeName = prefs[backgroundTypeKey]
        val savedColorArgb = prefs[colorTypeKey]
        val savedImageUriString = prefs[imageTypeKey]

        val savedFontColorArgb = prefs[fontColorKey]
        val savedFontSize = prefs[fontSizeKey]

        return WidgetConfiguration(
            soundId = savedSoundId ?: -1,
            backgroundType = BackgroundType.valueOf(savedBackgroundTypeName ?: BackgroundType.COLOR.name),
            backgroundColor = savedColorArgb?.let { Color(savedColorArgb) } ,
            backgroundImageUri = savedImageUriString?.toUri(),
            fontColor = savedFontColorArgb?.let { Color(it) } ?: Color.Black,
            fontSize = savedFontSize ?: 16f,
        )
    }
}