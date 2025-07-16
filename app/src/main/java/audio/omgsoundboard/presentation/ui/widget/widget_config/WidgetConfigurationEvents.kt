package audio.omgsoundboard.presentation.ui.widget.widget_config

import android.net.Uri
import androidx.compose.ui.graphics.Color
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.domain.models.BackgroundType


sealed class WidgetConfigurationEvents {
    data class OnLoadInitialState(val appWidgetId: Int) : WidgetConfigurationEvents()
    data class OnSoundSelected(val sound: PlayableSound) : WidgetConfigurationEvents()
    data class OnBackgroundTypeChange(val type: BackgroundType) : WidgetConfigurationEvents()
    data class OnColorSelected(val color: Color) : WidgetConfigurationEvents()
    data class OnFontColorSelected(val color: Color) : WidgetConfigurationEvents()
    data class OnFontSizeChange(val size: Float) : WidgetConfigurationEvents()
    data class OnCopyToInternalStorage(val uri: Uri, val filename: String) : WidgetConfigurationEvents()
}