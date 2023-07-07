package audio.omgsoundboard.presentation.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import androidx.wear.compose.material.rememberScalingLazyListState
import audio.omgsoundboard.core.R
import audio.omgsoundboard.presentation.composables.Chip
import audio.omgsoundboard.presentation.navigation.DrawerParams
import audio.omgsoundboard.presentation.navigation.Screens

@Composable
fun MainScreen(onNavigate: (Screens, String) -> Unit){

    val scalingLazyListState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        state = scalingLazyListState
    ){

        item {
            Text(text = stringResource(id = R.string.categories_header))
        }

        items(DrawerParams.drawerCategories){
            Chip(icon = it.drawableId, title = it.title) {
                onNavigate(it.screens, it.category)
            }
        }

        item {
            Text(text = stringResource(id = R.string.options_header))
        }

        items(DrawerParams.drawerOptions){
            Chip(icon = it.drawableId, title = it.title) {
                onNavigate(it.screens, it.category)
            }
        }
    }
}