package audio.omgsoundboard.di

import android.content.Context
import audio.omgsoundboard.data.UserPreferencesImpl
import audio.omgsoundboard.domain.repository.UserPreferences
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
    fun provideUserPreferences(@ApplicationContext context: Context) : UserPreferences {
        return UserPreferencesImpl(context)
    }

}