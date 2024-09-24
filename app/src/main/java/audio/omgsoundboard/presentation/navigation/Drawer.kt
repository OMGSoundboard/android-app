package audio.omgsoundboard.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.core.utils.Constants
import audio.omgsoundboard.core.utils.Constants.OPTIONS_PARTICLES
import audio.omgsoundboard.core.utils.Constants.OPTIONS_THEME_PICKER
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerContent(
    categories: List<Category>,
    drawerState: DrawerState,
    areParticlesEnable: Boolean,
    onCategory: (Category) -> Unit,
    onAction: (String) -> Unit,
) {

    var currentPick by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()


    ModalDrawerSheet {
        LazyColumn(
            horizontalAlignment = Alignment.Start
        ) {
            stickyHeader {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    text = stringResource(id = R.string.categories_header)
                )
            }
            items(categories) { item ->
                DrawerCategoryItem(
                    item = item,
                    isCurrent = currentPick == item.id,
                    isSelectable = true
                ) {

                    if (currentPick == item.id) {
                        return@DrawerCategoryItem
                    }

                    currentPick = item.id

                    coroutineScope.launch {
                        drawerState.close()
                    }

                    onCategory(item)
                }
            }
            stickyHeader {
                Text(
                    modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 16.dp),
                    text = stringResource(id = R.string.options_header)
                )
            }
            items(DrawerParams.drawerOptions) { item ->
                DrawerOptionItem(
                    item = item,
                    isCurrent = currentPick == item.id,
                    isSelectable = item.selectable,
                    switch = R.drawable.particles == item.drawableId,
                    areParticlesEnable = areParticlesEnable
                ) {

                    if (item.selectable){
                        if (currentPick == item.id) {
                            return@DrawerOptionItem
                        }

                        currentPick = item.id!!

                        coroutineScope.launch {
                            drawerState.close()
                        }

                        onAction(item.action!!)

                    } else {
                        onAction(item.action!!)
                    }
                }
            }
        }
    }
}


@Composable
fun DrawerCategoryItem(
    item: Category,
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
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = item.name,
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

@Composable
fun DrawerOptionItem(
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
    val id: Int?,
    val action: String?,
    val screens: Screens?,
    val selectable: Boolean,
    @StringRes val title: Int,
    @DrawableRes val drawableId: Int,
)

object DrawerParams {
    val drawerOptions = arrayListOf(
        DrawerItemModel(
            null,
            OPTIONS_PARTICLES,
            null,
            false,
            R.string.options_particles,
            R.drawable.particles,
        ),
        DrawerItemModel(
            null,
            OPTIONS_THEME_PICKER,
            null,
            false,
            R.string.options_theme_picker,
            R.drawable.color_picker,
        ),
        DrawerItemModel(
            -2,
            Constants.OPTIONS_ABOUT,
            Screens.AboutScreen,
            true,
            R.string.options_about,
            R.drawable.about,
        ),
    )
}
