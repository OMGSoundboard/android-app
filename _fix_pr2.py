from pathlib import Path
repo = Path(r"C:\Users\MMalmlu\Projects\OMGSoundboard-android-app")

p = repo / "app/src/main/java/audio/omgsoundboard/presentation/ui/sounds/SoundsScreen.kt"
t = p.read_text(encoding="utf-8")
block = """
    if (state.showSortPicker) {
        SortPicker(
            selectedSortOrder = state.soundSortOrder,
            onSortSelected = { sortOrder ->
                viewModel.onEvent(SoundsEvents.OnChangeSortOrder(sortOrder))
            },
            onDismiss = {
                viewModel.onEvent(SoundsEvents.OnShowHideSortPicker)
            }
        )
    }
"""
if "if (state.showSortPicker)" not in t:
    old = "            onDismiss = { viewModel.onEvent(SoundsEvents.OnShowHidePlaybackBehaviorDialog) }\n        )\n    }\n\n}"
    new = "            onDismiss = { viewModel.onEvent(SoundsEvents.OnShowHidePlaybackBehaviorDialog) }\n        )\n    }\n" + block + "\n}"
    if old not in t:
        raise SystemExit("SoundsScreen pattern not found")
    t = t.replace(old, new, 1)
    p.write_text(t, encoding="utf-8", newline="\n")

p = repo / "core/src/main/java/audio/omgsoundboard/core/data/StorageRepositoryImpl.kt"
t = p.read_text(encoding="utf-8")
if "playCount = soundBackup.playCount" not in t:
    t = t.replace(
        "                fileExtension = extension\n            )",
        "                fileExtension = extension,\n                playCount = soundBackup.playCount,\n            )",
        1,
    )
    p.write_text(t, encoding="utf-8", newline="\n")

p = repo / "wear/src/main/java/audio/omgsoundboard/DataLayerListenerService.kt"
t = p.read_text(encoding="utf-8")
if "playCount = soundJson" not in t:
    t = t.replace(
        "                        fileExtension = soundJson.optString(\"fileExtension\", DEFAULT_AUDIO_EXTENSION)\n                    )",
        "                        fileExtension = soundJson.optString(\"fileExtension\", DEFAULT_AUDIO_EXTENSION),\n                        playCount = soundJson.optInt(\"playCount\", 0),\n                    )",
        1,
    )
    p.write_text(t, encoding="utf-8", newline="\n")
print("fixes applied")
