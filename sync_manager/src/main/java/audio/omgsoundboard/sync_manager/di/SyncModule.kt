package audio.omgsoundboard.sync_manager.di

import audio.omgsoundboard.sync_manager.DataLayerRepository
import audio.omgsoundboard.sync_manager.DataLayerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class  SyncModule {
    @Binds
    @Singleton
    abstract fun bindDataLayerRepository(
        impl: DataLayerRepositoryImpl
    ): DataLayerRepository
}