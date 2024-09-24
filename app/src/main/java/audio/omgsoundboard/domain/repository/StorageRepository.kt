package audio.omgsoundboard.domain.repository

import audio.omgsoundboard.domain.models.UserPreferences
import kotlinx.coroutines.flow.Flow

interface StorageRepository {

    fun putStringPair(key: String, value: String)
    fun getStringPair(key: String, defaultValue: String): String

    fun putBooleanPair(key: String, value: Boolean)
    fun getBooleanPair(key: String, defaultValue: Boolean): Boolean

    fun getUserPreferencesAsFlow(): Flow<UserPreferences>
}