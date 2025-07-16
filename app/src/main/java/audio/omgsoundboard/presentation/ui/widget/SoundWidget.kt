package audio.omgsoundboard.presentation.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import audio.omgsoundboard.domain.models.BackgroundType
import audio.omgsoundboard.presentation.ui.widget.widget_config.SoundWidgetConfigureActivity
import audio.omgsoundboard.presentation.utils.backgroundTypeKey
import audio.omgsoundboard.presentation.utils.colorTypeKey
import audio.omgsoundboard.presentation.utils.decodeSampledBitmapFromFile
import audio.omgsoundboard.presentation.utils.fontColorKey
import audio.omgsoundboard.presentation.utils.fontSizeKey
import audio.omgsoundboard.presentation.utils.imageTypeKey
import audio.omgsoundboard.presentation.utils.soundIdPreferenceKey
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File


class SoundWidget: GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SoundProviderEntryPoint {
        fun soundsDao(): SoundsDao
        fun playerRepository(): PlayerRepository
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val hiltEntryPoint = EntryPointAccessors.fromApplication(context, SoundProviderEntryPoint::class.java)
        val soundsDao = hiltEntryPoint.soundsDao()

        provideContent {
            val soundId = currentState(key = soundIdPreferenceKey)
            val backgroundType = currentState(key = backgroundTypeKey)
            val backgroundColor = currentState(key = colorTypeKey) ?: Color.White.toArgb()
            val fontColor = currentState(key = fontColorKey) ?: Color.Black.toArgb()
            val fontSize = currentState(key = fontSizeKey) ?: 16f
            val backgroundImageUri = currentState(key = imageTypeKey)

            var sound by remember { mutableStateOf<PlayableSound?>(null) }
            LaunchedEffect(soundId) {
                sound = if (soundId != null) {
                    soundsDao.getSoundById(soundId)?.toDomain()
                } else {
                    null
                }
            }

            MyContent(
                sound,
                appWidgetId = appWidgetId,
                backgroundType = backgroundType,
                backgroundColor = backgroundColor,
                fontColor = fontColor,
                fontSize = fontSize,
                backgroundImageUri = backgroundImageUri,
            )
        }
    }

    @Composable
    private fun MyContent(
        sound: PlayableSound?,
        appWidgetId: Int,
        backgroundType: String?,
        backgroundColor: Int,
        fontColor: Int,
        fontSize: Float,
        backgroundImageUri: String?,
    ) {

        val defaultBackgroundColor = Color.White

        val imageProvider = if (backgroundType == BackgroundType.IMAGE.name) {
            backgroundImageUri?.let { uriString ->
                runCatching {
                    val filePath = uriString.toUri().path ?: return@runCatching null
                    val file = File(filePath)

                    if (file.exists()) {
                        val bitmap = decodeSampledBitmapFromFile(file, 512, 512)
                        bitmap?.let { ImageProvider(it) }
                    } else {
                        null
                    }
                }.getOrNull()
            }
        } else {
            null
        }

        val backgroundModifier = when (backgroundType) {
            BackgroundType.IMAGE.name -> {
                //The image composable bellow will cover this
                GlanceModifier.background(defaultBackgroundColor)
            }
            BackgroundType.COLOR.name -> {
                val color = Color(backgroundColor)
                GlanceModifier.background(ColorProvider(color, color))
            }
            else -> {
                GlanceModifier.background(defaultBackgroundColor)
            }
        }

        Box(
            modifier = GlanceModifier
                .then(backgroundModifier)
                .clickable(
                    onClick = if (sound == null) {
                        actionStartActivity<SoundWidgetConfigureActivity>()
                    } else {
                        actionRunCallback<PlaySoundActionCallback>(parameters = actionParametersOf(soundIdParamKey to sound.id))
                    }
                ),
            contentAlignment = Alignment.TopEnd
        ) {

            if (imageProvider != null && backgroundType == BackgroundType.IMAGE.name) {
                Image(
                    provider = imageProvider,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(10.dp)
                )
            }

            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sound?.title ?: "Tap to configure",
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = ColorProvider(Color(fontColor), Color(fontColor)),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (sound != null) {
                Image(
                    provider = ImageProvider(R.drawable.settings),
                    colorFilter = ColorFilter.tint(ColorProvider(Color(fontColor), Color(fontColor))),
                    contentDescription = "Configure",
                    modifier = GlanceModifier
                        .padding(8.dp)
                        .clickable(
                            onClick = actionStartActivity<SoundWidgetConfigureActivity>(
                                parameters = actionParametersOf(
                                    ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID) to appWidgetId
                                )
                            )
                        )
                )
            }
        }
    }
}

val soundIdParamKey = ActionParameters.Key<Int>("soundId")

class PlaySoundActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val soundId = parameters[soundIdParamKey] ?: return

        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SoundWidget.SoundProviderEntryPoint::class.java
        )

        val soundsDao = hiltEntryPoint.soundsDao()
        val playerRepository = hiltEntryPoint.playerRepository()

        val soundEntity = soundsDao.getSoundById(soundId)

        soundEntity?.let {
            playerRepository.playFile(it.id, it.resId, it.uri)
        }
    }
}


class SoundWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SoundWidget()
}