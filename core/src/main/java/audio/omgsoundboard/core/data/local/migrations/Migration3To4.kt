package audio.omgsoundboard.core.data.local.migrations

import androidx.room.migration.Migration

/** Tracks how often each sound has been played. */
val Migration3To4: Migration =
    addSoundsIntegerColumnMigration(3, 4, "play_count", 0)
