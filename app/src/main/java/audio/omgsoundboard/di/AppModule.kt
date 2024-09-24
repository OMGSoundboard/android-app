package audio.omgsoundboard.di

import android.content.Context
import audio.omgsoundboard.data.StorageRepositoryImpl
import audio.omgsoundboard.domain.repository.StorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesStorage(@ApplicationContext context: Context) : StorageRepository {
        return StorageRepositoryImpl(context)
    }

}