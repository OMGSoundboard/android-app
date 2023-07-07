package audio.omgsoundboard.presentation.theme

import android.os.Build

enum class ThemeType {
    DARK,
    LIGHT,
    DYNAMIC,
    SYSTEM
}

fun toThemeType(type: String): ThemeType{
    return when(type){
        "DARK" ->  ThemeType.DARK
        "LIGHT" -> ThemeType.LIGHT
        "DYNAMIC" -> ThemeType.DYNAMIC
        "SYSTEM" -> ThemeType.SYSTEM
        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {ThemeType.DYNAMIC} else {ThemeType.SYSTEM}
    }
}