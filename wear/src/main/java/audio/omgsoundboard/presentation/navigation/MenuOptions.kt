package audio.omgsoundboard.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.utils.Constants


data class DrawerItemModel(
    val screens: Screens?,
    val action: String,
    @StringRes val title: Int,
    @DrawableRes val drawableId: Int,
)

object DrawerParams {
    val drawerOptions = arrayListOf(
        DrawerItemModel(
            Screens.AboutScreen,
            Constants.OPTIONS_ABOUT,
            R.string.options_about,
            R.drawable.about,
        ),
        DrawerItemModel(
            null,
            Constants.OPTIONS_SYNC,
            R.string.sync,
            R.drawable.sync,
        ),
    )
}