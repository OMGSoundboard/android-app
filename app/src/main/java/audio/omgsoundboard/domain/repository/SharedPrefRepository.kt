package audio.omgsoundboard.domain.repository

import audio.omgsoundboard.domain.models.UserPreferences
import audio.omgsoundboard.domain.models.WidgetConfiguration
import kotlinx.coroutines.flow.Flow

interface SharedPrefRepository {

    fun putStringPair(key: String, value: String)
    fun getStringPair(key: String, defaultValue: String): String

    fun putBooleanPair(key: String, value: Boolean)
    fun getBooleanPair(key: String, defaultValue: Boolean): Boolean

    fun putIntPair(key: String, value: Int)
    fun getIntPair(key: String, defaultValue: Int): Int

    fun getUserPreferencesAsFlow(): Flow<UserPreferences>
    suspend fun getWidgetPreferences(widgetId: Int): WidgetConfiguration
}