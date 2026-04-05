
package com.valentinegarage.data.model

data class ServiceTask(
    val taskId: String = "",
    val checkInId: String = "",
    val taskDescription: String = "",
    val isCompleted: Boolean = false,
    val completedByEmployeeId: String? = null,
    val mechanicNotes: String = "",
    val completionTimestamp: Long? = null
)

