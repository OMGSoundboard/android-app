@file:OptIn(ExperimentalMaterial3Api::class)

package audio.omgsoundboard.presentation.ui.sounds

import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.utils.Constants.OPTIONS_ABOUT
import audio.omgsoundboard.core.utils.Constants.OPTIONS_PARTICLES
import audio.omgsoundboard.core.utils.Constants.OPTIONS_THEME_PICKER
import audio.omgsoundboard.presentation.composables.AddSoundDialog
import audio.omgsoundboard.presentation.composables.DropMenu
import audio.omgsoundboard.presentation.composables.InfoDialog
import audio.omgsoundboard.presentation.composables.MyTextField
import audio.omgsoundboard.presentation.composables.PermissionDialog
import audio.omgsoundboard.presentation.composables.ThemePicker
import audio.omgsoundboard.presentation.navigation.DrawerContent
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.utils.UiEvent
import kotlinx.coroutines.launch


@Composable
fun SoundsScreen(
    onNavigate: (String) -> Unit,
    viewModel: SoundsViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event.route)
                else -> Unit
            }
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
                        OPTIONS_ABOUT -> {
                            viewModel.onEvent(SoundsEvents.OnNavigate(it))
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
        SoundsScreenContent(state, drawerState, viewModel::onEvent)
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

            itemsIndexed(state.sounds) { index, sound ->
                SoundItem(
                    item = sound,
                    index = index,
                    onFav = {
                        onEvents(SoundsEvents.OnToggleFav(sound.id))
                    },
                    onPlay = {
                        onEvents(SoundsEvents.OnPlaySound(index, sound.resId, sound.uri))
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
                onEvents(SoundsEvents.OnShowHideRenameSoundDialog(pickedSound.title))
            },
            onDelete = {
                onEvents(SoundsEvents.OnShowHideDeleteSoundDialog)
            },
            onDismiss = {
                onEvents(SoundsEvents.OnToggleDropMenu)
            }
        )
    }

    if (state.showRenameSoundDialog){
        AddSoundDialog(
            isRename = true,
            text = state.textFieldValue,
            onChange = {
                onEvents(SoundsEvents.OnTextFieldChange(it))
            },
            error = state.textFieldError,
            onFinish = {
                onEvents(SoundsEvents.OnConfirmRename(pickedSound))
            },
            onDismiss = {
                onEvents(SoundsEvents.OnShowHideRenameSoundDialog(""))
            }
        )
    }

    if (state.showConfirmDeleteDialog){
        InfoDialog(
            text = stringResource(R.string.delete_sound_confirm),
            onConfirmation = {
                onEvents(SoundsEvents.OnConfirmDelete(pickedSound.id))
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


@Composable
fun SoundItem(
    item: PlayableSound,
    index: Int,
    onFav: () -> Unit,
    onPlay: () -> Unit,
    onDropMenu: (Offset) -> Unit,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 6.dp)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        scope.launch {
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                        onPlay()
                    },
                    onLongPress = {
                        onDropMenu(it)
                    }
                )
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (index % 2 == 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    .width(3.dp)
                    .height(60.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    text = item.title,
                )
                IconButton(onClick = onFav) {
                    Icon(
                        painter = painterResource(
                            id = if (item.isFav) {
                                R.drawable.fav
                            } else {
                                R.drawable.fav_outlined
                            }
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}




