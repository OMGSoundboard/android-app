package audio.omgsoundboard.presentation.ui.widget.widget_config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.domain.models.BackgroundType
import audio.omgsoundboard.presentation.composables.ColorSelector
import audio.omgsoundboard.presentation.theme.OMGSoundboardTheme
import audio.omgsoundboard.presentation.ui.widget.SoundWidget
import audio.omgsoundboard.presentation.utils.backgroundTypeKey
import audio.omgsoundboard.presentation.utils.colorTypeKey
import audio.omgsoundboard.presentation.utils.fontColorKey
import audio.omgsoundboard.presentation.utils.fontSizeKey
import audio.omgsoundboard.presentation.utils.imageTypeKey
import audio.omgsoundboard.presentation.utils.soundIdPreferenceKey
import coil3.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SoundWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            OMGSoundboardTheme {
                ConfigureScreen(
                    appWidgetId = appWidgetId,
                    onConfigurationComplete = ::handleConfigurationComplete
                )
            }
        }
    }

    private fun handleConfigurationComplete(
        sound: PlayableSound,
        backgroundType: BackgroundType,
        backgroundColor: Color?,
        fontColor: Color,
        fontSize: Float,
        backgroundImageUri: Uri?
    ) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@SoundWidgetConfigureActivity).getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@SoundWidgetConfigureActivity, glanceId) { prefs ->
                prefs[soundIdPreferenceKey] = sound.id

                prefs[fontColorKey] = fontColor.toArgb()
                prefs[fontSizeKey] = fontSize

                prefs[backgroundTypeKey] = backgroundType.name
                if (backgroundType == BackgroundType.COLOR) {
                    backgroundColor?.let { prefs[colorTypeKey] = it.toArgb() }
                    prefs.remove(imageTypeKey)
                } else {
                    backgroundImageUri?.let { prefs[imageTypeKey] = it.toString() }
                    prefs.remove(colorTypeKey)
                }
            }

            SoundWidget().update(this@SoundWidgetConfigureActivity, glanceId)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureScreen(
    appWidgetId: Int,
    onConfigurationComplete: (
        sound: PlayableSound,
        backgroundType: BackgroundType,
        backgroundColor: Color?,
        fontColor: Color,
        fontSize: Float,
        backgroundImageUri: Uri?
    ) -> Unit,
    viewModel: WidgetConfigurationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvents(WidgetConfigurationEvents.OnLoadInitialState(appWidgetId))
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvents(WidgetConfigurationEvents.OnCopyToInternalStorage(uri, "widget_bg_$appWidgetId.png"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Widget") },
                actions = {
                    if (state.selectedSound != null) {
                        TextButton(
                            onClick = {
                                state.selectedSound?.let { sound ->
                                    onConfigurationComplete(
                                        sound,
                                        state.backgroundType,
                                        if (state.backgroundType == BackgroundType.COLOR) state.selectedColor else null,
                                        state.fontColor,
                                        state.fontSize,
                                        if (state.backgroundType == BackgroundType.IMAGE) state.pickedImageUri else null
                                    )
                                }
                            }
                        ) {
                            Text("Done")
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text("Sound") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { Text("Preview & Background") }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SoundSelectionTab(
                        sounds = state.sounds,
                        selectedSound = state.selectedSound,
                        onSoundSelected = {
                            viewModel.onEvents(WidgetConfigurationEvents.OnSoundSelected(it))
                        }
                    )

                    1 -> PreviewAndBackgroundTab(
                        state = state,
                        onEvents = viewModel::onEvents,
                        onImagePick = { imagePickerLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}

@Composable
fun SoundSelectionTab(
    sounds: List<PlayableSound>,
    selectedSound: PlayableSound?,
    onSoundSelected: (PlayableSound) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Select a sound for your widget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(sounds, key = { it.id }) { sound ->
            SoundSelectionItem(
                sound = sound,
                isSelected = selectedSound?.id == sound.id,
                onClick = { onSoundSelected(sound) }
            )
        }
    }
}

@Composable
fun PreviewAndBackgroundTab(
    state: WidgetConfigurationState,
    onEvents: (WidgetConfigurationEvents) -> Unit,
    onImagePick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Widget Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            WidgetPreview(
                sound = state.selectedSound,
                backgroundType = state.backgroundType,
                backgroundColor = state.selectedColor,
                fontColor = state.fontColor,
                fontSize = state.fontSize,
                imageUri = state.pickedImageUri
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Background",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.backgroundType == BackgroundType.COLOR,
                onClick = {
                    onEvents(WidgetConfigurationEvents.OnBackgroundTypeChange(BackgroundType.COLOR))
                },
                label = { Text("Color") },
                leadingIcon = {
                    Icon(Icons.Default.Palette, contentDescription = null)
                }
            )
            FilterChip(
                selected = state.backgroundType == BackgroundType.IMAGE,
                onClick = {
                    onEvents(WidgetConfigurationEvents.OnBackgroundTypeChange(BackgroundType.IMAGE))
                    onImagePick()
                },
                label = { Text("Image") },
                leadingIcon = {
                    Icon(Icons.Default.Image, contentDescription = null)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (state.backgroundType) {
            BackgroundType.COLOR -> {
                ColorSelector(
                    selectedColor = state.selectedColor,
                    onColorSelected = {
                        onEvents(WidgetConfigurationEvents.OnColorSelected(it))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            BackgroundType.IMAGE -> {}
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Text Style",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("Font Size: ${state.fontSize.toInt()} sp")
        Slider(
            value = state.fontSize,
            onValueChange = {
                onEvents(WidgetConfigurationEvents.OnFontSizeChange(it))
            },
            valueRange = 12f..32f,
            steps = 19
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("Font Color")
        Spacer(modifier = Modifier.height(8.dp))
        ColorSelector(
            selectedColor = state.fontColor,
            onColorSelected = {
                onEvents(WidgetConfigurationEvents.OnFontColorSelected(it))
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun WidgetPreview(
    sound: PlayableSound?,
    backgroundType: BackgroundType,
    backgroundColor: Color?,
    fontColor: Color,
    fontSize: Float,
    imageUri: Uri?
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                when (backgroundType) {
                    BackgroundType.COLOR -> Modifier.background(backgroundColor ?: Color.White)
                    BackgroundType.IMAGE -> Modifier.background(Color.White)

                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (backgroundType == BackgroundType.IMAGE && imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = sound?.title ?: "",
            color = fontColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SoundSelectionItem(
    sound: PlayableSound,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = sound.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}