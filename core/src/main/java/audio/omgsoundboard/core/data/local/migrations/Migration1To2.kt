package audio.omgsoundboard.core.data.local.migrations

import android.content.Context
import android.net.Uri
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import audio.omgsoundboard.core.R

class Migration1To2(private val context: Context) : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create new tables
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sounds (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                uri TEXT NOT NULL,
                date INTEGER NOT NULL,
                isFavorite INTEGER NOT NULL DEFAULT 0,
                category_id INTEGER,
                res_id INTEGER,
                FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE SET NULL
            )
        """)

        // Create index on category_id in sounds table
        db.execSQL("CREATE INDEX index_sounds_category_id ON sounds(category_id)")

        // Seed categories
        val categories = listOf("Funny", "Games", "Movies", "Music", "Custom")
        categories.forEach { category ->
            db.execSQL("INSERT INTO categories (name) VALUES (?)", arrayOf(category))
        }

        val funnyCategory = db.execSQL("SELECT id FROM categories WHERE name = 'Funny'")
        val currentTimeMillis = System.currentTimeMillis()

        val titles = context.resources.getStringArray(R.array.seeded_sounds)
        val resIds = context.resources.obtainTypedArray(R.array.seeded_sounds_ids)

        for (i in titles.indices) {
            val title = titles[i]
            val resId = resIds.getResourceId(i, 0)
            val uri = Uri.EMPTY

            db.execSQL(
                "INSERT INTO sounds (title, uri, date, category_id, res_id, isFavorite) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(title, uri, currentTimeMillis, funnyCategory, resId, 0)
            )
        }

        resIds.recycle()

        // Move data from custom_sounds to sounds
        db.execSQL("""
            INSERT INTO sounds (title, uri, date, category_id)
            SELECT title, uri, date, (SELECT id FROM categories WHERE name = 'Custom')
            FROM CustomSounds
        """)

        // Update favorites
        db.execSQL("""
            UPDATE sounds
            SET isFavorite = 1
            WHERE res_id IN (
                SELECT resId
                FROM Favorites
            ) OR uri IN (
                SELECT uri
                FROM Favorites
            )
        """)

        // Drop old tables and rename new favorites
        db.execSQL("DROP TABLE CustomSounds")
        db.execSQL("DROP TABLE Favorites")
    }
}