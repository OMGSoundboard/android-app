package audio.omgsoundboard.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.utils.Constants


data class DrawerItemModel(
    val screens: Screens,
    val category: String,
    val selectable: Boolean,
    @StringRes val title: Int,
    @DrawableRes val drawableId: Int,
)

object DrawerParams {
    val drawerCategories = arrayListOf(
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            Constants.CATEGORY_ALL,
            true,
            R.string.category_all,
            R.drawable.all,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            Constants.CATEGORY_FUNNY,
            true,
            R.string.category_funny,
            R.drawable.funny,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            Constants.CATEGORY_GAMES,
            true,
            R.string.category_games,
            R.drawable.games,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            Constants.CATEGORY_MOVIES,
            true,
            R.string.category_movies,
            R.drawable.movies,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            Constants.CATEGORY_MUSIC,
            true,
            R.string.category_music,
            R.drawable.music,
        ),
        DrawerItemModel(
            Screens.FavoritesScreen,
            Constants.CATEGORY_FAVORITES,
            true,
            R.string.favorites_title,
           R.drawable.fav_outlined,
        )
    )
    val drawerOptions = arrayListOf(
        DrawerItemModel(
            Screens.AboutScreen,
            Constants.OPTIONS_ABOUT,
            true,
            R.string.options_about,
            R.drawable.about,
        ),
    )
}