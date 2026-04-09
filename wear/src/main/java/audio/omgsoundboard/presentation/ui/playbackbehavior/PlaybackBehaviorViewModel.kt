package audio.omgsoundboard.presentation.ui.playbackbehavior

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import audio.omgsoundboard.core.utils.Constants.STOP_ON_NEW_SOUND
import audio.omgsoundboard.core.utils.Constants.STOP_ON_RETAP
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class PlaybackBehaviorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    var uiState by mutableStateOf(PlaybackBehaviorState())
        private set

    init {
        uiState = uiState.copy(
            stopOnRetap = sharedPref.getBoolean(STOP_ON_RETAP, false),
            stopOnNewSound = sharedPref.getBoolean(STOP_ON_NEW_SOUND, false),
        )
    }

    fun toggleStopOnRetap() {
        val new = !uiState.stopOnRetap
        sharedPref.edit().putBoolean(STOP_ON_RETAP, new).apply()
        uiState = uiState.copy(stopOnRetap = new)
    }

    fun toggleStopOnNewSound() {
        val new = !uiState.stopOnNewSound
        sharedPref.edit().putBoolean(STOP_ON_NEW_SOUND, new).apply()
        uiState = uiState.copy(stopOnNewSound = new)
    }
}
