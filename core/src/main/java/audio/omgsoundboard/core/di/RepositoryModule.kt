package audio.omgsoundboard.core.di

import android.content.Context
import audio.omgsoundboard.core.data.PlayerRepositoryImpl
import audio.omgsoundboard.core.data.StorageRepositoryImpl
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import audio.omgsoundboard.core.domain.repository.StorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

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
    ): StorageRepository {
        return StorageRepositoryImpl(
            context,
        )
    }
}