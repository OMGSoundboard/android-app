package audio.omgsoundboard.presentation.navigation

enum class Screens (var title: String, val route: String, val searchable: Boolean = true, val showFav: Boolean = true) {
     OnboardingScreen ( title = "Onboarding", route = "onboarding_screen_route", searchable = false, showFav = false),
     SoundsScreen ( title = "OMGSoundboard", route = "sounds_screen_route" ),
     FavoritesScreen (title = "Favorites", route = "favorites_screen_route",  showFav = false),
     AboutScreen( title = "About", route = "about_screen_route", searchable = false)
}

object Graph {
     const val ONBOARDING = "onboarding"
     const val HOME = "home"
}