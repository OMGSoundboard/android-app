package audio.omgsoundboard.di

import android.content.Context
import audio.omgsoundboard.data.PlayerRepositoryImpl
import audio.omgsoundboard.data.StorageRepositoryImpl
import audio.omgsoundboard.data.UserPreferencesImpl
import audio.omgsoundboard.data.local.SoundsDatabase
import audio.omgsoundboard.domain.repository.PlayerRepository
import audio.omgsoundboard.domain.repository.StorageRepository
import audio.omgsoundboard.domain.repository.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserPreferences(@ApplicationContext context: Context) : UserPreferences {
        return UserPreferencesImpl(context)
    }

    @Provides
    @Singleton
    fun providesPlayerRepository(
        @ApplicationContext context: Context,
    ): PlayerRepository {
        return PlayerRepositoryImpl(
            context
        )
    }

    @Provides
    @Singleton
    fun providesStorageRepository(
        @ApplicationContext context: Context,
        db: SoundsDatabase,
    ): StorageRepository {
        return StorageRepositoryImpl(
            context,
            db.getFavoritesDao(),
            db.getCustomSoundsDao()
        )
    }

}