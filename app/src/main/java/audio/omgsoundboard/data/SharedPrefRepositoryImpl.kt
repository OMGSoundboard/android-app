package audio.omgsoundboard.data

import android.content.Context
import android.content.SharedPreferences
import audio.omgsoundboard.core.utils.Constants.PARTICLES_STATUS
import audio.omgsoundboard.core.utils.Constants.THEME_TYPE
import audio.omgsoundboard.domain.models.UserPreferences
import audio.omgsoundboard.domain.repository.SharedPrefRepository
import audio.omgsoundboard.presentation.theme.ThemeType
import audio.omgsoundboard.presentation.theme.toThemeType
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
}