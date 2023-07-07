package audio.omgsoundboard.core.di

import android.content.Context
import androidx.room.Room
import audio.omgsoundboard.core.data.local.SoundsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        ).build()

    @Singleton
    @Provides
    fun providesFavoritesDao(db: SoundsDatabase) = db.getFavoritesDao()

    @Singleton
    @Provides
    fun providesCustomSoundsDao(db: SoundsDatabase) = db.getCustomSoundsDao()
}