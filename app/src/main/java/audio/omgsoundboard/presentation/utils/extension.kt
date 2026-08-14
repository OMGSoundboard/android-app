package audio.omgsoundboard.presentation.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import java.io.File


/** Combines seven flows into a single transformed flow. */
inline fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> {
    return kotlinx.coroutines.flow.combine(flow, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
        )
    }
}

inline fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return kotlinx.coroutines.flow.combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
        )
    }
}

fun decodeSampledBitmapFromFile(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
    // First, decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, this)

        inSampleSize = calculateInSampleSizeForCrop(this, reqWidth, reqHeight)

        inJustDecodeBounds = false
        BitmapFactory.decodeFile(file.absolutePath, this)
    }
}

private fun calculateInSampleSizeForCrop(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((height / (inSampleSize * 2)) >= reqHeight && (width / (inSampleSize * 2)) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

val soundIdPreferenceKey = intPreferencesKey("widget_sound_id")
val backgroundTypeKey = stringPreferencesKey("widget_background_type")
val colorTypeKey = intPreferencesKey("widget_color_type")
val imageTypeKey = stringPreferencesKey("widget_image_type")
val fontColorKey = intPreferencesKey("widget_font_color")
val fontSizeKey = floatPreferencesKey("widget_font_size")
