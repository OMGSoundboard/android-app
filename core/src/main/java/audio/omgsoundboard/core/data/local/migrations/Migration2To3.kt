package audio.omgsoundboard.core.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration2To3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE sounds
            ADD COLUMN file_extension TEXT NOT NULL DEFAULT 'mp3'
            """.trimIndent()
        )
    }
}
