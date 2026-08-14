package audio.omgsoundboard.core.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration3To4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE sounds
            ADD COLUMN play_count INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}
