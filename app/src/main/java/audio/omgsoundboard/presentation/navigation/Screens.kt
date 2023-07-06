package audio.omgsoundboard.presentation.navigation

enum class Screens (var title: String, val route: String, val searchable: Boolean = true, val showFav: Boolean = true) {

     OnboardingScreen ( title = "Onboarding", route = "onboarding_screen_route", searchable = false, showFav = false),
     CategorySoundsScreen ( title = "OMGSoundboard", route = "category_sounds_screen_route" ),
     FavoritesScreen (title = "Favorites", route = "favorites_screen_route",  showFav = false),
     CustomScreen (title = "Custom", route = "custom_screen_route"),
     AboutScreen( title = "About", route = "about_screen_route", searchable = false)
}