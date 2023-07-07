package audio.omgsoundboard.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [FavoritesEntity::class, CustomSoundsEntity::class], version = 1, exportSchema = false)
@TypeConverters(UriTypeConverter::class)
abstract class SoundsDatabase: RoomDatabase(){

    abstract fun getFavoritesDao() : FavoritesDao
    abstract fun getCustomSoundsDao() : CustomSoundsDao

}