package audio.omgsoundboard.presentation.theme

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
        else -> ThemeType.SYSTEM
    }
}