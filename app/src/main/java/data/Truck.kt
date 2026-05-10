package data

import kotlinx.serialization.Serializable

@Serializable
data class Truck(
    val id: String? = null,
    val plate_number: String,
    val owner_name: String,
    val condition: String,
    val kilometers: Int
)