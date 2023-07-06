package audio.omgsoundboard.presentation.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import audio.omgsoundboard.R
import audio.omgsoundboard.presentation.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    viewModel: MainViewModel, navController: NavController, openDrawer: () -> Unit
) {

    var showSearchBar by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    if (showSearchBar) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = viewModel.searchText,
            onValueChange = viewModel::setSearchTextValue,
            singleLine = true,
            leadingIcon = {
                IconButton(onClick = {
                    viewModel.setSearchTextValue("")
                    showSearchBar = false
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    viewModel.setSearchTextValue("")
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            )
        )
        LaunchedEffect(Unit){
            focusRequester.requestFocus()
        }
    } else {
        if (viewModel.currentScreen != Screens.OnboardingScreen){
            TopAppBar(title = {
                Text(
                    text = viewModel.currentScreen.title,
                )
            }, navigationIcon = {
                if (viewModel.currentScreen == Screens.FavoritesScreen) {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                } else {
                    IconButton(onClick = openDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                        )
                    }
                }
            }, actions = {
                if (viewModel.currentScreen.showFav) {
                    IconButton(onClick = {
                        navController.navigate(Screens.FavoritesScreen.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (viewModel.currentScreen.searchable) {
                    IconButton(onClick = {
                        showSearchBar = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                        )
                    }
                }
                if (viewModel.currentScreen == Screens.CustomScreen) {
                    OverflowMenu {
                        DropdownMenuItem(text = {
                            Text(text = stringResource(id = R.string.export_backup))
                        }, onClick = {
                            viewModel.backupPress(export = true, restore = null)
                        })
                        DropdownMenuItem(text = {
                            Text(text = stringResource(id = R.string.restore_backup))
                        }, onClick = {
                            viewModel.backupPress(export = null, restore = true)
                        })
                    }
                }
            })
        }
    }
}

@Composable
fun OverflowMenu(content: @Composable () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
        )
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        content()
    }
}