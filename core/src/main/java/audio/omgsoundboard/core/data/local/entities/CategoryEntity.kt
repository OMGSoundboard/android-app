package audio.omgsoundboard.core.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.core.utils.Constants.CATEGORIES_TABLE

@Entity(tableName = CATEGORIES_TABLE)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name") val name: String
)


fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name
)