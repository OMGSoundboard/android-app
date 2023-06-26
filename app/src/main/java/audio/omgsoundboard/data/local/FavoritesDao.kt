package audio.omgsoundboard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import audio.omgsoundboard.utils.Constants.FAVORITES_TABLE

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM $FAVORITES_TABLE")
    suspend fun getFavorites(): List<FavoritesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(sound: FavoritesEntity)

    @Query("DELETE FROM $FAVORITES_TABLE WHERE id = :favoriteId")
    suspend fun deleteFavorite(favoriteId: Int)

}