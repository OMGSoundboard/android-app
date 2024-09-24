package audio.omgsoundboard.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.CategoryEntity
import audio.omgsoundboard.core.data.local.entities.SoundsEntity


@Database(
    entities = [SoundsEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(UriTypeConverter::class)
abstract class SoundsDatabase: RoomDatabase(){
    abstract fun getCategoryDao(): CategoryDao
    abstract fun getSoundDao(): SoundsDao
}