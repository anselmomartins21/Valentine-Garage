package com.valentinegarage.data

// This is our data model - it represents one truck check-in
// Think of it like a form with all the truck's information
data class VehicleCheckIn(
    val checkInId: String = "",           // unique ID for this check-in
    val licensePlate: String = "",         // truck plate number
    val kilometersDriven: Int = 0,        // km on the odometer
    val initialCondition: String = "",    // condition when it arrived
    val driverName: String = "",          // who brought the truck
    val driverPhone: String = "",         // driver contact number
    val timestamp: Long = System.currentTimeMillis(), // when it was checked in
    val status: String = "In Progress"    // In Progress or Completed
)
