import re
from pathlib import Path
repo = Path(r"C:\Users\MMalmlu\Projects\OMGSoundboard-android-app")

def read(p): return p.read_text(encoding="utf-8")
def write(p, s): p.write_text(s, encoding="utf-8", newline="\n")

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/local/SoundsDatabase.kt"
write(p, read(p).replace("version = 4", "version = 3"))

p = repo / "core/src/main/java/audio/omgsoundboard/core/di/RoomModule.kt"
t = read(p)
t = t.replace("import audio.omgsoundboard.core.data.local.migrations.Migration3To4\n", "")
t = t.replace(", Migration3To4", "")
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/local/entities/SoundsEntity.kt"
t = read(p)
t = re.sub(r",\n    @ColumnInfo\(name = \"play_count\"\) val playCount: Int = 0,", ",", t)
t = t.replace(",\n    playCount = playCount,", "")
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/local/daos/SoundsDao.kt"
t = read(p)
t = re.sub(r"\n    @Query\(\"UPDATE \$SOUNDS_TABLE SET play_count = play_count \+ 1 WHERE id = :soundId\"\)\n    suspend fun incrementPlayCount\(soundId: Int\)\n", "\n", t)
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/utils/Constants.kt"
t = read(p)
t = t.replace("    const val SOUND_SORT_ORDER = \"SoundSortOrder\"\n\n", "")
t = t.replace("    const val OPTIONS_SORT = \"Sort\"\n\n", "")
write(p, t)

for rel in [
    "core/src/main/java/audio/omgsoundboard/core/domain/models/PlayableSound.kt",
    "core/src/main/java/audio/omgsoundboard/core/domain/models/SoundBackup.kt",
]:
    p = repo / rel
    t = read(p)
    t = re.sub(r",\n    val playCount: Int = 0,", ",", t)
    t = t.replace(",\n    playCount = playCount,", "")
    write(p, t)

p = repo / "core/src/main/res/values/strings.xml"
t = read(p)
t = re.sub(r"\n    <string name=\"options_sort\">Sort</string>\n    <string name=\"sort_button\">Sort sounds</string>\n    <string name=\"sort_title\">Sort sounds</string>\n    <string name=\"sort_alpha_asc\">A to Z</string>\n    <string name=\"sort_alpha_desc\">Z to A</string>\n    <string name=\"sort_most_used\">Most used</string>\n    <string name=\"sort_recently_added\">Recently added</string>", "", t)
write(p, t)

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/StorageRepositoryImpl.kt"
t = read(p)
t = t.replace(",\n                playCount = soundBackup.playCount,", "")
write(p, t)

p = repo / "wear/src/main/java/audio/omgsoundboard/DataLayerListenerService.kt"
t = read(p)
t = t.replace(",\n                        playCount = soundJson.optInt(\"playCount\", 0),", "")
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/utils/extension.kt"
t = read(p)
t = re.sub(r"inline fun <T1, T2, T3, T4, T5, T6, T7, R> combine\([\s\S]*?\n\}\n\n", "", t)
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsState.kt"
t = read(p)
t = t.replace("import audio.omgsoundboard.core.domain.models.SoundSortOrder\n", "")
t = re.sub(r"\n    val showSortPicker: Boolean = false,\n    val soundSortOrder: SoundSortOrder = SoundSortOrder\.TITLE_ASC,", "", t)
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsEvents.kt"
t = read(p)
t = t.replace("import audio.omgsoundboard.core.domain.models.SoundSortOrder\n", "")
t = re.sub(r"\n    object OnShowHideSortPicker : SoundsEvents\(\)\n    data class OnChangeSortOrder\(val sortOrder: SoundSortOrder\) : SoundsEvents\(\)", "", t)
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsScreen.kt"
t = read(p)
for line in [
    "import androidx.compose.material.icons.filled.Sort\n",
    "import audio.omgsoundboard.presentation.composables.SortPicker\n",
]:
    t = t.replace(line, "")
t = re.sub(r"\n    if \(state\.showSortPicker\) \{[\s\S]*?\n    \}\n", "\n", t)
t = re.sub(r"\n                            IconButton\(\n                                onClick = \{\n                                    onEvents\(SoundsEvents\.OnShowHideSortPicker\)\n                                \}\n                            \) \{\n                                Icon\(\n                                    imageVector = Icons\.Default\.Sort,\n                                    contentDescription = stringResource\(id = R\.string\.sort_button\),\n                                \)\n                            \}\n", "\n", t)
write(p, t)

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsViewModel.kt"
t = read(p)
for imp in [
    "import audio.omgsoundboard.core.utils.Constants.SOUND_SORT_ORDER\n",
    "import audio.omgsoundboard.core.domain.models.SoundSortOrder\n",
    "import audio.omgsoundboard.core.domain.models.sortedBy\n",
    "import audio.omgsoundboard.core.domain.models.toSoundSortOrder\n",
]:
    t = t.replace(imp, "")
t = t.replace("    private val _sortOrder = MutableStateFlow(SoundSortOrder.TITLE_ASC)\n", "")
t = t.replace("        _wearNodes,\n        _sortOrder,\n    ) { state, categories, categoryId, sounds, search, wearNodes, sortOrder ->", "        _wearNodes\n    ) { state, categories, categoryId, sounds, search, wearNodes ->")
t = t.replace("            sounds = sounds.map { it.toDomain() }.sortedBy(sortOrder),", "            sounds = sounds.map { it.toDomain() },")
t = t.replace("            wearNodes = wearNodes,\n            soundSortOrder = sortOrder,", "            wearNodes = wearNodes,")
t = re.sub(r"\n            is SoundsEvents\.OnShowHideSortPicker -> \{[\s\S]*?\n            \}\n\n            is SoundsEvents\.OnChangeSortOrder -> \{[\s\S]*?\n            \}", "", t)
t = re.sub(r"\n        if \(index > 0\) \{\n            viewModelScope\.launch \{\n                soundsDao\.incrementPlayCount\(index\)\n            \}\n        \}", "", t)
t = re.sub(r"\n    private fun changeSortOrder\(sortOrder: SoundSortOrder\) \{[\s\S]*?\n    \}\n", "\n", t)
t = t.replace("            val sortOrderPref = shared.getStringPair(SOUND_SORT_ORDER, SoundSortOrder.TITLE_ASC.name)\n\n            _sortOrder.value = toSoundSortOrder(sortOrderPref)\n            ", "            ")
write(p, t)

print("PR1 strip done")
