package audio.omgsoundboard.presentation.navigation

enum class Screens (var title: String, val route: String) {
     OnboardingScreen ( title = "Onboarding", route = "onboarding_screen_route"),
     CategoriesScreen( title = "Categories", route = "categories_screen_route"),
     SoundsScreen ( title = "OMGSoundboard", route = "sounds_screen_route" ),
     FavoritesScreen (title = "Favorites", route = "favorites_screen_route"),
     AboutScreen( title = "About", route = "about_screen_route")
}

object Graph {
     const val ONBOARDING = "onboarding"
     const val HOME = "home"
}