package audio.omgsoundboard.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.Constants.SOUNDS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: SoundsEntity): Long

    @Query("UPDATE $SOUNDS_TABLE SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFav(id: Int)

    @Update
    suspend fun updateSound(sound: SoundsEntity)

    @Query("DELETE FROM $SOUNDS_TABLE WHERE id = :soundId")
    suspend fun deleteSound(soundId: Int)

    @Query("SELECT * FROM $SOUNDS_TABLE WHERE category_id = :categoryId")
    fun getSoundsByCategoryId(categoryId: Int?): Flow<List<SoundsEntity>>

    @Query("SELECT * FROM $SOUNDS_TABLE WHERE category_id = :categoryId AND title LIKE '%' || :query || '%'")
    fun searchSoundByCategory(categoryId: Int?, query: String): Flow<List<SoundsEntity>>

    @Query("SELECT * FROM $SOUNDS_TABLE WHERE title LIKE '%' || :query || '%'")
    fun searchAllSounds(query: String): Flow<List<SoundsEntity>>

    @Query("SELECT * FROM $SOUNDS_TABLE")
    fun getAllSounds(): Flow<List<SoundsEntity>>

    @Query("SELECT * FROM $SOUNDS_TABLE WHERE isFavorite = 1")
    fun getAllFavorites(): Flow<List<SoundsEntity>>
}