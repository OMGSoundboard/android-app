package audio.omgsoundboard.presentation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.utils.Constants.ONBOARDING_SHOWN
import audio.omgsoundboard.domain.repository.StorageRepository
import audio.omgsoundboard.presentation.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val storage: StorageRepository,
) : ViewModel() {


    var keepSplash by mutableStateOf(true)
        private set

    var onboardingShown by mutableStateOf(true)
        private set

    var selectedTheme by mutableStateOf(ThemeType.DARK)
        private set

    var areParticlesEnabled by mutableStateOf(false)
        private set


    init {
        readUserPreferences()
    }


    fun setOnboardingAsShown(){
        storage.putBooleanPair(ONBOARDING_SHOWN, true)
        onboardingShown = true
    }


    private fun readUserPreferences() {
        viewModelScope.launch {
            launch {
                storage.getUserPreferencesAsFlow().collect { userPreferences ->
                    selectedTheme = userPreferences.selectedTheme
                    areParticlesEnabled = userPreferences.areParticlesEnabled
                }
            }

            launch {
                onboardingShown = storage.getBooleanPair(ONBOARDING_SHOWN, false)
            }

            delay(500)
            keepSplash = false
        }
    }
}