package audio.omgsoundboard.presentation.navigation

enum class Screens (var title: String, val route: String, val searchable: Boolean, val showFav: Boolean = true) {

    MenuScreen ( title = "OMGSoundboard", route = "menu_screen_route", searchable = false),
    CategorySoundsScreen ( title = "OMGSoundboard", route = "category_sounds_screen_route", searchable = true),
    FavoritesScreen (title = "Favorites", route = "favorites_screen_route", searchable = true, showFav = false),
    CustomScreen (title = "Custom", route = "custom_screen_route", searchable = true, showFav = true),
    AboutScreen( title = "About", route = "about_screen_route", searchable = false)

}