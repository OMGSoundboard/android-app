package audio.omgsoundboard.domain.models

import android.net.Uri
import androidx.compose.ui.graphics.Color

data class WidgetConfiguration(
    val soundId: Int,
    val backgroundType: BackgroundType,
    val backgroundColor: Color?,
    val backgroundImageUri: Uri?,
    val fontColor: Color,
    val fontSize: Float,
)
