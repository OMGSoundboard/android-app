package audio.omgsoundboard.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import audio.omgsoundboard.core.utils.Constants

@Dao
interface CustomSoundsDao {

    @Query("SELECT * FROM ${Constants.CUSTOM_SOUNDS_TABLE}")
    suspend fun getCustomSounds(): List<CustomSoundsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomSound(sound: CustomSoundsEntity)

    @Query("DELETE FROM ${Constants.CUSTOM_SOUNDS_TABLE} WHERE id = :customSoundId")
    suspend fun deleteCustomSound(customSoundId: Int)
}