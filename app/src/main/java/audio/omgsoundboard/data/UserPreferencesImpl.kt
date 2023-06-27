package audio.omgsoundboard.data

import android.content.Context
import audio.omgsoundboard.domain.repository.UserPreferences
import javax.inject.Inject

class UserPreferencesImpl @Inject constructor(
    private val context: Context
) : UserPreferences {

    private val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    override fun putStringPair(key: String, value: String) {
        with (sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }
    override fun getStringPair(key: String) : String {
        return sharedPref?.getString(key, "") ?: ""
    }

    override fun putBooleanPair(key: String, value: Boolean) {
        with (sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }
    override fun getBooleanPair(key: String) : Boolean{
        return sharedPref?.getBoolean(key, false) ?: false
    }

}