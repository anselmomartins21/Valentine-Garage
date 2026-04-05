package com.valentinegarage.data.model

data class VehicleCheckIn(
    val checkInId: String = "",
    val licensePlate: String = "",
    val kilometersDriven: Int = 0,
    val initialCondition: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val checkInTimestamp: Long = System.currentTimeMillis(),
    val checkedInByEmployeeId: String = "",
    val status: String = "In Progress"
)
