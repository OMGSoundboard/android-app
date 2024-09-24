package audio.omgsoundboard.core.domain.models

import audio.omgsoundboard.core.data.local.entities.CategoryEntity

data class Category(
    val id: Int,
    val name: String
)


fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name
)
