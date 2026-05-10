package data

import kotlinx.serialization.Serializable

@Serializable
data class GarageTask(
    val id: String? = null,
    val truck_id: String,
    val mechanic_id: String,
    val task_description: String,
    val status: String,
    val notes: String? = null
)