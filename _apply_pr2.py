import re
import shutil
from pathlib import Path

repo = Path(r"C:\Users\MMalmlu\Projects\OMGSoundboard-android-app")
sort_backup = Path(r"C:\Users\MMalmlu\Projects\_pr2_sort_backup")

def read(p): return p.read_text(encoding="utf-8")
def write(p, s): p.write_text(s, encoding="utf-8", newline="\n")

for rel in [
    "app/src/main/java/audio/omgsoundboard/presentation/composables/SortPicker.kt",
    "core/src/main/java/audio/omgsoundboard/core/data/local/migrations/Migration3To4.kt",
    "core/src/main/java/audio/omgsoundboard/core/domain/models/SoundSortOrder.kt",
    "core/src/test/java/audio/omgsoundboard/core/domain/models/SoundSortOrderTest.kt",
]:
    src = sort_backup / rel
    dst = repo / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/local/SoundsDatabase.kt"
write(p, read(p).replace("version = 3", "version = 4"))

p = repo / "core/src/main/java/audio/omgsoundboard/core/di/RoomModule.kt"
t = read(p)
if "Migration3To4" not in t:
    t = t.replace(
        "import audio.omgsoundboard.core.data.local.migrations.Migration2To3\n",
        "import audio.omgsoundboard.core.data.local.migrations.Migration2To3\nimport audio.omgsoundboard.core.data.local.migrations.Migration3To4\n",
    )
    t = t.replace("Migration2To3)", "Migration2To3, Migration3To4)")
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/local/entities/SoundsEntity.kt"
t = read(p)
if "play_count" not in t:
    t = t.replace(
        "    @ColumnInfo(name = \"file_extension\") val fileExtension: String = DEFAULT_AUDIO_EXTENSION,\n)",
        "    @ColumnInfo(name = \"file_extension\") val fileExtension: String = DEFAULT_AUDIO_EXTENSION,\n    @ColumnInfo(name = \"play_count\") val playCount: Int = 0,\n)",
    )
    t = t.replace(
        "    fileExtension = fileExtension,\n)",
        "    fileExtension = fileExtension,\n    playCount = playCount,\n)",
    )
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/local/daos/SoundsDao.kt"
t = read(p)
if "incrementPlayCount" not in t:
    t = t.replace(
        "    suspend fun toggleFav(id: Int)\n",
        "    suspend fun toggleFav(id: Int)\n\n    @Query(\"UPDATE $SOUNDS_TABLE SET play_count = play_count + 1 WHERE id = :soundId\")\n    suspend fun incrementPlayCount(soundId: Int)\n",
    )
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/utils/Constants.kt"
t = read(p)
if "SOUND_SORT_ORDER" not in t:
    t = t.replace(
        '    const val STOP_ON_NEW_SOUND = "StopOnNewSound"\n',
        '    const val STOP_ON_NEW_SOUND = "StopOnNewSound"\n    const val SOUND_SORT_ORDER = "SoundSortOrder"\n',
    )
    t = t.replace(
        '    const val OPTIONS_PLAYBACK_BEHAVIOR = "PlaybackBehavior"\n',
        '    const val OPTIONS_PLAYBACK_BEHAVIOR = "PlaybackBehavior"\n    const val OPTIONS_SORT = "Sort"\n',
    )
write(p, t)

for rel in [
    "core/src/main/java/audio/omgsoundboard/core/domain/models/PlayableSound.kt",
    "core/src/main/java/audio/omgsoundboard/core/domain/models/SoundBackup.kt",
]:
    p = repo / rel
    t = read(p)
    if "playCount" not in t:
        t = t.replace(
            "    val fileExtension: String = DEFAULT_AUDIO_EXTENSION,\n)",
            "    val fileExtension: String = DEFAULT_AUDIO_EXTENSION,\n    val playCount: Int = 0,\n)",
        )
        t = t.replace(
            "    fileExtension = fileExtension,\n)",
            "    fileExtension = fileExtension,\n    playCount = playCount,\n)",
        )
    write(p, t)

p = repo / "core/src/main/res/values/strings.xml"
t = read(p)
if "options_sort" not in t:
    t = t.replace(
        '    <string name="options_playback_behavior">Playback behavior</string>\n',
        '    <string name="options_playback_behavior">Playback behavior</string>\n'
        '    <string name="options_sort">Sort</string>\n'
        '    <string name="sort_button">Sort sounds</string>\n'
        '    <string name="sort_title">Sort sounds</string>\n'
        '    <string name="sort_alpha_asc">A to Z</string>\n'
        '    <string name="sort_alpha_desc">Z to A</string>\n'
        '    <string name="sort_most_used">Most used</string>\n'
        '    <string name="sort_recently_added">Recently added</string>\n',
    )
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/StorageRepositoryImpl.kt"
t = read(p)
if "playCount = soundBackup.playCount" not in t:
    t = t.replace(
        "                fileExtension = extension,\n            )",
        "                fileExtension = extension,\n                playCount = soundBackup.playCount,\n            )",
    )
write(p, t)

p = repo / "wear/src/main/java/audio/omgsoundboard/DataLayerListenerService.kt"
t = read(p)
if "playCount" not in t:
    t = t.replace(
        '                        fileExtension = soundJson.optString("fileExtension", DEFAULT_AUDIO_EXTENSION),\n                    )',
        '                        fileExtension = soundJson.optString("fileExtension", DEFAULT_AUDIO_EXTENSION),\n                        playCount = soundJson.optInt("playCount", 0),\n                    )',
    )
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/utils/extension.kt"
t = read(p)
if "T7" not in t:
    seven = '''
inline fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> {
    return kotlinx.coroutines.flow.combine(flow, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
        )
    }
}

'''
    t = t.replace("import java.io.File\n\n", "import java.io.File\n\n" + seven)
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsState.kt"
t = read(p)
if "SoundSortOrder" not in t:
    t = t.replace(
        "import audio.omgsoundboard.core.domain.models.PlayableSound\n",
        "import audio.omgsoundboard.core.domain.models.PlayableSound\nimport audio.omgsoundboard.core.domain.models.SoundSortOrder\n",
    )
    t = t.replace(
        "    val showPlaybackBehaviorDialog: Boolean = false,\n)",
        "    val showPlaybackBehaviorDialog: Boolean = false,\n    val showSortPicker: Boolean = false,\n    val soundSortOrder: SoundSortOrder = SoundSortOrder.TITLE_ASC,\n)",
    )
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsEvents.kt"
t = read(p)
if "OnShowHideSortPicker" not in t:
    t = t.replace(
        "import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION\n",
        "import audio.omgsoundboard.core.domain.models.SoundSortOrder\nimport audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION\n",
    )
    t = t.replace(
        "    object OnToggleStopOnNewSound : SoundsEvents()\n}",
        "    object OnToggleStopOnNewSound : SoundsEvents()\n    object OnShowHideSortPicker : SoundsEvents()\n    data class OnChangeSortOrder(val sortOrder: SoundSortOrder) : SoundsEvents()\n}",
    )
write(p, t)

# SoundsViewModel
p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsViewModel.kt"
t = read(p)
if "_sortOrder" not in t:
    t = t.replace(
        "import audio.omgsoundboard.core.utils.Constants.PARTICLES_STATUS\n",
        "import audio.omgsoundboard.core.utils.Constants.PARTICLES_STATUS\nimport audio.omgsoundboard.core.utils.Constants.SOUND_SORT_ORDER\n",
    )
    t = t.replace(
        "import audio.omgsoundboard.core.utils.existingSoundFileKeys\n",
        "import audio.omgsoundboard.core.utils.existingSoundFileKeys\nimport audio.omgsoundboard.core.domain.models.SoundSortOrder\nimport audio.omgsoundboard.core.domain.models.sortedBy\nimport audio.omgsoundboard.core.domain.models.toSoundSortOrder\n",
    )
    t = t.replace(
        "    private val _categoryId = MutableStateFlow(-1)\n",
        "    private val _categoryId = MutableStateFlow(-1)\n    private val _sortOrder = MutableStateFlow(SoundSortOrder.TITLE_ASC)\n",
    )
    t = t.replace(
        "        _wearNodes\n    ) { state, categories, categoryId, sounds, search, wearNodes ->",
        "        _wearNodes,\n        _sortOrder,\n    ) { state, categories, categoryId, sounds, search, wearNodes, sortOrder ->",
    )
    t = t.replace(
        "            sounds = sounds.map { it.toDomain() },",
        "            sounds = sounds.map { it.toDomain() }.sortedBy(sortOrder),",
    )
    t = t.replace(
        "            wearNodes = wearNodes,\n        )",
        "            wearNodes = wearNodes,\n            soundSortOrder = sortOrder,\n        )",
    )
    t = t.replace(
        "            is SoundsEvents.OnToggleStopOnNewSound -> {\n                toggleStopOnNewSound()\n            }\n        }",
        "            is SoundsEvents.OnToggleStopOnNewSound -> {\n                toggleStopOnNewSound()\n            }\n\n            is SoundsEvents.OnShowHideSortPicker -> {\n                _state.value = _state.value.copy(showSortPicker = !_state.value.showSortPicker)\n            }\n\n            is SoundsEvents.OnChangeSortOrder -> {\n                changeSortOrder(event.sortOrder)\n            }\n        }",
    )
    t = t.replace(
        "    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {\n        player.playFile(index, resourceId, uri)\n    }",
        "    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {\n        player.playFile(index, resourceId, uri)\n        if (index > 0) {\n            viewModelScope.launch {\n                soundsDao.incrementPlayCount(index)\n            }\n        }\n    }",
    )
    t = t.replace(
        "    private fun toggleStopOnRetap() {",
        "    private fun changeSortOrder(sortOrder: SoundSortOrder) {\n        viewModelScope.launch {\n            shared.putStringPair(SOUND_SORT_ORDER, sortOrder.name)\n            _sortOrder.value = sortOrder\n        }\n    }\n\n    private fun toggleStopOnRetap() {",
    )
    t = t.replace(
        "            val stopOnNewSound = shared.getBooleanPair(STOP_ON_NEW_SOUND, false)\n\n            _state.value = _state.value.copy(",
        "            val stopOnNewSound = shared.getBooleanPair(STOP_ON_NEW_SOUND, false)\n            val sortOrderPref = shared.getStringPair(SOUND_SORT_ORDER, SoundSortOrder.TITLE_ASC.name)\n\n            _sortOrder.value = toSoundSortOrder(sortOrderPref)\n            _state.value = _state.value.copy(",
    )
write(p, t)

# SoundsScreen sort UI
p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsScreen.kt"
t = read(p)
if "SortPicker" not in t:
    t = t.replace(
        "import androidx.compose.material.icons.filled.Search\n",
        "import androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.Sort\n",
    )
    t = t.replace(
        "import audio.omgsoundboard.presentation.composables.SoundItem\n",
        "import audio.omgsoundboard.presentation.composables.SoundItem\nimport audio.omgsoundboard.presentation.composables.SortPicker\n",
    )
    t = t.replace(
        "            onDismiss = { viewModel.onEvent(SoundsEvents.OnShowHidePlaybackBehaviorDialog) }\n        )\n    }\n}",
        "            onDismiss = { viewModel.onEvent(SoundsEvents.OnShowHidePlaybackBehaviorDialog) }\n        )\n    }\n\n    if (state.showSortPicker) {\n        SortPicker(\n            selectedSortOrder = state.soundSortOrder,\n            onSortSelected = { sortOrder ->\n                viewModel.onEvent(SoundsEvents.OnChangeSortOrder(sortOrder))\n            },\n            onDismiss = {\n                viewModel.onEvent(SoundsEvents.OnShowHideSortPicker)\n            }\n        )\n    }\n}",
    )
    t = t.replace(
        "                            IconButton(onClick = {\n                                onEvents(SoundsEvents.OnToggleSearch)\n                            }) {",
        "                            IconButton(\n                                onClick = {\n                                    onEvents(SoundsEvents.OnShowHideSortPicker)\n                                }\n                            ) {\n                                Icon(\n                                    imageVector = Icons.Default.Sort,\n                                    contentDescription = stringResource(id = R.string.sort_button),\n                                )\n                            }\n                            IconButton(onClick = {\n                                onEvents(SoundsEvents.OnToggleSearch)\n                            }) {",
    )
write(p, t)

# SoundItem ellipsis UI
p = repo / "app/src/main/java/audio/omgsoundboard/presentation/composables/SoundItem.kt"
t = read(p)
if "TextOverflow" not in t:
    t = t.replace(
        "import androidx.compose.ui.res.painterResource\n",
        "import androidx.compose.ui.res.painterResource\nimport androidx.compose.ui.text.style.TextOverflow\n",
    )
    t = t.replace(
        """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        text = item.title,
                    )""",
        """                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        text = item.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )""",
    )
write(p, t)

print("PR2 apply done")
