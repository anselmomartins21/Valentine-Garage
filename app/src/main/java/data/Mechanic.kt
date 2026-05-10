package data

import kotlinx.serialization.Serializable

@Serializable
data class Mechanic(
    val id: String? = null,
    val name: String,
    val email: String,
    val role: String
)