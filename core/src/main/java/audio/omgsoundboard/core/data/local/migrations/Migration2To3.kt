package audio.omgsoundboard.core.data.local.migrations

import androidx.room.migration.Migration

/** Adds persisted file extensions for imported sounds. */
val Migration2To3: Migration =
    addSoundsTextColumnMigration(2, 3, "file_extension", "mp3")
