package com.valentinegarage.data.repository

import com.valentinegarage.data.model.VehicleCheckIn

interface GarageRepository {
    suspend fun checkInVehicle(checkIn: VehicleCheckIn)
}

