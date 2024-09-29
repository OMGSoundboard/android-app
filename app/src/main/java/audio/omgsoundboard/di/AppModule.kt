package audio.omgsoundboard.di

import android.content.Context
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.data.DataLayerRepositoryImpl
import audio.omgsoundboard.data.SharedPrefRepositoryImpl
import audio.omgsoundboard.domain.repository.DataLayerRepository
import audio.omgsoundboard.domain.repository.SharedPrefRepository
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
    fun providesStorage(@ApplicationContext context: Context) : SharedPrefRepository {
        return SharedPrefRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun providesDataLayerRepository(
        @ApplicationContext context: Context,
        categoryDao: CategoryDao,
        soundsDao: SoundsDao,
    ) : DataLayerRepository {
        return DataLayerRepositoryImpl(context, categoryDao, soundsDao)
    }
}