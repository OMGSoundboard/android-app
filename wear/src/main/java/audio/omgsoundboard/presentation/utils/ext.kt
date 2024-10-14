package audio.omgsoundboard.presentation.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

fun Modifier.fillMaxRectangle(): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "fillMaxRectangle"
    },
) {
    val isRound = LocalConfiguration.current.isScreenRound
    var inset: Dp = 0.dp
    if (isRound) {
        val screenHeightDp = LocalConfiguration.current.screenHeightDp
        val screenWidthDp = LocalConfiguration.current.smallestScreenWidthDp
        val maxSquareEdge = (sqrt(((screenHeightDp * screenWidthDp) / 2).toDouble()))
        inset = Dp(((screenHeightDp - maxSquareEdge) / 2).toFloat())
    }
    fillMaxSize().padding(all = inset)
}