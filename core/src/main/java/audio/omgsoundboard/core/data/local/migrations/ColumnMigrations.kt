package audio.omgsoundboard.core.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds a non-null TEXT column with a default value to the sounds table. */
internal fun addSoundsTextColumnMigration(
    fromVersion: Int,
    toVersion: Int,
    columnName: String,
    defaultValue: String,
): Migration = object : Migration(fromVersion, toVersion) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE sounds
            ADD COLUMN $columnName TEXT NOT NULL DEFAULT '$defaultValue'
            """.trimIndent()
        )
    }
}

/** Adds a non-null INTEGER column with a default value to the sounds table. */
internal fun addSoundsIntegerColumnMigration(
    fromVersion: Int,
    toVersion: Int,
    columnName: String,
    defaultValue: Int,
): Migration = object : Migration(fromVersion, toVersion) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE sounds
            ADD COLUMN $columnName INTEGER NOT NULL DEFAULT $defaultValue
            """.trimIndent()
        )
    }
}
