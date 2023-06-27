package audio.omgsoundboard.domain.repository

interface UserPreferences {

    fun putStringPair(key: String, value: String)
    fun getStringPair(key: String): String

    fun putBooleanPair(key: String, value: Boolean)
    fun getBooleanPair(key: String): Boolean

}