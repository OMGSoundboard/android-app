@file:OptIn(ExperimentalMaterial3Api::class)

package audio.omgsoundboard.presentation.ui.sounds

import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.utils.Constants.OPTIONS_ABOUT
import audio.omgsoundboard.core.utils.Constants.OPTIONS_CATEGORY
import audio.omgsoundboard.core.utils.Constants.OPTIONS_PARTICLES
import audio.omgsoundboard.core.utils.Constants.OPTIONS_THEME_PICKER
import audio.omgsoundboard.presentation.composables.AddRenameDialog
import audio.omgsoundboard.presentation.composables.DropMenu
import audio.omgsoundboard.presentation.composables.Fab
import audio.omgsoundboard.presentation.composables.InfoDialog
import audio.omgsoundboard.presentation.composables.MyTextField
import audio.omgsoundboard.presentation.composables.PermissionDialog
import audio.omgsoundboard.presentation.composables.SoundItem
import audio.omgsoundboard.presentation.composables.ThemePicker
import audio.omgsoundboard.presentation.navigation.DrawerContent
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.utils.UiEvent
import audio.omgsoundboard.presentation.utils.getTitleFromUri
import kotlinx.coroutines.launch


@Composable
fun SoundsScreen(
    onNavigate: (String) -> Unit,
    viewModel: SoundsViewModel = hiltViewModel(),
) {

    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowInfoDialog -> {
                    showInfoDialog = true
                    infoDialogText = event.message.asString(context)
                }
                is UiEvent.Navigate -> onNavigate(event.route)
                else -> Unit
            }
        }
    }

    val soundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { soundUri ->
        if (soundUri != null) {
            val pickedSoundTitle = getTitleFromUri(context, soundUri) ?: ""
            viewModel.onEvent(SoundsEvents.OnShowHideAddRenameSoundDialog(pickedSoundTitle, false, soundUri))
        }
    }

    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                categories = state.categories,
                drawerState = drawerState,
                areParticlesEnable = state.areParticlesEnable,
                onCategory = { category ->
                    viewModel.onEvent(SoundsEvents.OnSetCategoryId(category.id))
                },
                onAction = {
                    when (it) {
                        OPTIONS_CATEGORY -> {
                            viewModel.onEvent(SoundsEvents.OnNavigate(Screens.CategoriesScreen.route))
                        }

                        OPTIONS_ABOUT -> {
                            viewModel.onEvent(SoundsEvents.OnNavigate(Screens.AboutScreen.route))
                        }

                        OPTIONS_PARTICLES -> {
                            viewModel.onEvent(SoundsEvents.OnToggleParticles)
                        }

                        OPTIONS_THEME_PICKER -> {
                            viewModel.onEvent(SoundsEvents.OnShowHideThemePicker)
                        }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SoundsScreenContent(state, drawerState, viewModel::onEvent)
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = state.currentCategory?.id != -1, enter = slideInVertically() + expandVertically(
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    initialAlpha = 0.3f
                ), exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically()
            ) {
                Fab(
                    modifier = Modifier.padding(12.dp),
                ) {
                    soundPicker.launch("audio/mpeg")
                }
            }

        }
    }

    if (showInfoDialog) {
        InfoDialog(
            text = infoDialogText,
            onDismissRequest = {
                showInfoDialog = false
            },
            onConfirmation = {
                showInfoDialog = false
            }
        )
    }

    if (state.showThemePicker) {
        ThemePicker(
            selectedThemeType = state.pickedTheme,
            pickTheme = { theme ->
                viewModel.onEvent(SoundsEvents.OnChangeTheme(theme))
            },
            onDismiss = {
                viewModel.onEvent(SoundsEvents.OnShowHideThemePicker)
            }
        )
    }
}

@Composable
fun SoundsScreenContent(
    state: SoundsState,
    drawerState: DrawerState,
    onEvents: (SoundsEvents) -> Unit,
) {

    val context = LocalContext.current
    var hasWriteSettingsPermission by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    var pickedSound by remember { mutableStateOf(PlayableSound()) }

    Column {
        Crossfade(state.showSearchField, label = "TopBar") { show ->
            when (show) {
                true -> {
                    MyTextField(
                        searchTerm = state.searchTerm,
                        onValueChange = {
                            onEvents(SoundsEvents.OnSearchTerm(it))
                        },
                        cancelSearch = {
                            onEvents(SoundsEvents.OnToggleSearch)
                        }
                    )
                }

                false -> {
                    TopAppBar(
                        title = {
                            Text(text = state.currentCategory?.name ?: "")
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null,
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onEvents(SoundsEvents.OnNavigate(Screens.FavoritesScreen.route))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = {
                                onEvents(SoundsEvents.OnToggleSearch)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                )
                            }
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            item {
                if (state.sounds.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.no_sounds_here_yet),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp)
                    )
                }
            }

            itemsIndexed(state.sounds, key = { _, sound -> sound.id }) { index, sound ->
                SoundItem(
                    item = sound,
                    index = index,
                    onFav = {
                        onEvents(SoundsEvents.OnToggleFav(sound.id))
                    },
                    onPlay = {
                        onEvents(SoundsEvents.OnPlaySound(sound.id, sound.resId, sound.uri))
                    },
                    onDropMenu = {
                        touchPoint = it
                        pickedSound = sound
                        onEvents(SoundsEvents.OnToggleDropMenu)
                    }
                )
            }
        }
    }

    if (state.showDropMenu) {
        DropMenu(
            touchPoint = touchPoint,
            hasWriteSettingsPermission = hasWriteSettingsPermission,
            askForPermission = {
                showPermissionDialog = true
            },
            onShare = {
                onEvents(SoundsEvents.OnShareSound(pickedSound))
            },
            onSetAsRingtone = {
                onEvents(SoundsEvents.OnSetAsRingtone(pickedSound))
            },
            onSetAsAlarm = {
                onEvents(SoundsEvents.OnSetAsAlarm(pickedSound))
            },
            onSetAsNotification = {
                onEvents(SoundsEvents.OnSetAsNotification(pickedSound))
            },
            onRename = {
                onEvents(SoundsEvents.OnShowHideAddRenameSoundDialog(pickedSound.title, true))
            },
            onDelete = {
                onEvents(SoundsEvents.OnShowHideDeleteSoundDialog)
            },
            onDismiss = {
                onEvents(SoundsEvents.OnToggleDropMenu)
            }
        )
    }

    if (state.showAddRenameSoundDialog){
        AddRenameDialog(
            title = stringResource(id = if (state.isRenaming)  R.string.update_sound else R.string.sound_add_title),
            placeholderText = stringResource(id = R.string.sound_add_placeholder),
            buttonText = stringResource(id = if (state.isRenaming) R.string.save else  R.string.add),
            text = state.textFieldValue,
            onChange = {
                onEvents(SoundsEvents.OnTextFieldChange(it))
            },
            error = state.textFieldError,
            onFinish = {
                if (state.isRenaming){
                    onEvents(SoundsEvents.OnConfirmRename(pickedSound))
                } else {
                    onEvents(SoundsEvents.OnConfirmAdd)
                }
            },
            onDismiss = {
                onEvents(SoundsEvents.OnShowHideAddRenameSoundDialog("", false))
            }
        )
    }

    if (state.showConfirmDeleteDialog){
        InfoDialog(
            text = stringResource(R.string.delete_sound_confirm),
            onConfirmation = {
                onEvents(SoundsEvents.OnConfirmDelete(pickedSound.id))
                onEvents(SoundsEvents.OnShowHideDeleteSoundDialog)
            },
            onDismissRequest = {
                onEvents(SoundsEvents.OnShowHideDeleteSoundDialog)
            }
        )
    }

    if (showPermissionDialog) {
        PermissionDialog(
            result = {
                hasWriteSettingsPermission = it
                showPermissionDialog = false
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }
}






