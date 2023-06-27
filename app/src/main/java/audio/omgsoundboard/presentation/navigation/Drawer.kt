package audio.omgsoundboard.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.R
import audio.omgsoundboard.utils.Constants
import audio.omgsoundboard.utils.Constants.CATEGORY_ALL
import audio.omgsoundboard.utils.Constants.CATEGORY_CUSTOM
import audio.omgsoundboard.utils.Constants.CATEGORY_FUNNY
import audio.omgsoundboard.utils.Constants.CATEGORY_GAMES
import audio.omgsoundboard.utils.Constants.CATEGORY_MOVIES
import audio.omgsoundboard.utils.Constants.CATEGORY_MUSIC
import audio.omgsoundboard.utils.Constants.OPTIONS_PARTICLES
import audio.omgsoundboard.utils.Constants.OPTIONS_THEME_PICKER
import kotlinx.coroutines.launch


@Composable
fun DrawerContent(
    drawerState: DrawerState,
    defaultPick: DrawerItemModel,
    areParticlesEnable: Boolean,
    onClick: (DrawerItemModel) -> Unit
) {

    var currentPick by remember { mutableStateOf(defaultPick) }
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = stringResource(id = R.string.categories_header)
        )

        LazyColumn(
            horizontalAlignment = Alignment.Start
        ) {
            items(DrawerParams.drawerCategories) { item ->
                DrawerItem(
                    item = item,
                    isCurrent = currentPick == item,
                    isSelectable = item.selectable
                ) {

                    if (currentPick == item) {
                        return@DrawerItem
                    }

                    currentPick = item

                    coroutineScope.launch {
                        drawerState.close()
                    }

                    onClick(item)
                }
            }
        }

        Text(
            modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 16.dp),
            text = stringResource(id = R.string.options_header)
        )

        LazyColumn(
            horizontalAlignment = Alignment.Start
        ) {
            items(DrawerParams.drawerOptions) { item ->
                DrawerItem(
                    item = item,
                    isCurrent = currentPick == item,
                    isSelectable = item.selectable,
                    switch = R.drawable.particles == item.drawableId,
                    areParticlesEnable = areParticlesEnable
                ) {

                    if (item.selectable){
                        if (currentPick == item) {
                            return@DrawerItem
                        }

                        currentPick = item

                        coroutineScope.launch {
                            drawerState.close()
                        }

                        onClick(item)

                    } else {
                        onClick(item)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    item: DrawerItemModel,
    isCurrent: Boolean,
    isSelectable: Boolean,
    switch: Boolean = false,
    areParticlesEnable: Boolean = false,
    onClick: () -> Unit
) {

    val color = if (isCurrent) {
        if (isSelectable){
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = Modifier
            .width(250.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(16.dp),
        ) {
            Icon(
                painter = painterResource(id = item.drawableId),
                tint = color,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = item.title),
                color = color
            )
            if (switch){
                Switch(modifier = Modifier.height(8.dp),checked = areParticlesEnable, onCheckedChange  = {
                    onClick()
                } )
            }
        }
    }
}


data class DrawerItemModel(
    val screens: Screens?,
    val category: String?,
    val selectable: Boolean,
    @StringRes val title: Int,
    @DrawableRes val drawableId: Int,
)

object DrawerParams {
    val drawerCategories = arrayListOf(
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            CATEGORY_ALL,
            true,
            R.string.category_all,
            R.drawable.all,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            CATEGORY_FUNNY,
            true,
            R.string.category_funny,
            R.drawable.funny,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            CATEGORY_GAMES,
            true,
            R.string.category_games,
            R.drawable.games,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            CATEGORY_MOVIES,
            true,
            R.string.category_movies,
            R.drawable.movies,
        ),
        DrawerItemModel(
            Screens.CategorySoundsScreen,
            CATEGORY_MUSIC,
            true,
            R.string.category_music,
            R.drawable.music,
        ),
        DrawerItemModel(
            Screens.CustomScreen,
            CATEGORY_CUSTOM,
            true,
            R.string.category_custom,
            R.drawable.music,
        )
    )
    val drawerOptions = arrayListOf(
        DrawerItemModel(
            null,
            OPTIONS_PARTICLES,
            false,
            R.string.options_particles,
            R.drawable.particles,
        ),
        DrawerItemModel(
            null,
            OPTIONS_THEME_PICKER,
            false,
            R.string.options_theme_picker,
            R.drawable.color_picker,
        ),
        DrawerItemModel(
            Screens.AboutScreen,
            Constants.OPTIONS_ABOUT,
            true,
            R.string.options_about,
            R.drawable.about,
        ),
    )

}
