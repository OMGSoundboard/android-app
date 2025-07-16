package audio.omgsoundboard.presentation.ui.widget.widget_config


import android.net.Uri
import androidx.compose.ui.graphics.Color
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.domain.models.BackgroundType

data class WidgetConfigurationState(
    val sounds: List<PlayableSound> = emptyList(),
    val selectedSound: PlayableSound? = null,
    val backgroundType: BackgroundType = BackgroundType.COLOR,
    val selectedColor: Color? = null,
    val pickedImageUri: Uri? = null,
    val fontColor: Color = Color.White,
    val fontSize: Float = 16f
)
