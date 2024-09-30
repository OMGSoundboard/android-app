package audio.omgsoundboard.domain.models

import audio.omgsoundboard.presentation.theme.ThemeType

data class UserPreferences(
    val selectedTheme: ThemeType,
    val areParticlesEnabled: Boolean,
)
