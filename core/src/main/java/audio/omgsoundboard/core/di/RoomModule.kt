package audio.omgsoundboard.core.di

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.data.local.SoundsDatabase
import audio.omgsoundboard.core.data.local.entities.CategoryEntity
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.data.local.migrations.Migration1To2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private const val OMG_SOUNDBOARD_DATABASE = "OMGSoundBoardDatabase"

    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            SoundsDatabase::class.java,
            OMG_SOUNDBOARD_DATABASE
        )
            .addMigrations(Migration1To2(context))
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(context)
                    }
                }
            })
            .build()


    @Singleton
    @Provides
    fun providesCategoryDao(db: SoundsDatabase) = db.getCategoryDao()

    @Singleton
    @Provides
    fun providesSoundDao(db: SoundsDatabase) = db.getSoundDao()

    private suspend fun seedDatabase(context: Context) {
        val database = provideRoom(context)
        val categoryDao = database.getCategoryDao()
        val soundDao = database.getSoundDao()

        val categories = listOf("Funny", "Games", "Movies", "Music", "Custom")
        val entities = categories.map { CategoryEntity(name = it) }
        categoryDao.insertCategories(entities)

        val funnyCategory = categoryDao.getCategoryByName("Funny")

        // Seed predefined sounds
        val titles = context.resources.getStringArray(R.array.seeded_sounds)
        val resIds = context.resources.obtainTypedArray(R.array.seeded_sounds_ids)

        for (i in titles.indices) {
            val title = titles[i]
            val resId = resIds.getResourceId(i, 0)
            soundDao.insertSound(
                SoundsEntity(
                    title = title,
                    uri = Uri.EMPTY,
                    date = System.currentTimeMillis(),
                    categoryId = funnyCategory.id,
                    isFavorite = false,
                    resId = resId
                )
            )
        }

        resIds.recycle()
    }
}