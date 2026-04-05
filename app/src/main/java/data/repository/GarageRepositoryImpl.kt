package com.valentinegarage.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.valentinegarage.data.model.ServiceTask
import com.valentinegarage.data.model.VehicleCheckIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val DEFAULT_SERVICE_TASKS = listOf(
    "Inspect engine oil level",
    "Check coolant level",
    "Inspect tyre pressure & condition",
    "Check brake fluid",
    "Inspect brake pads / discs",
    "Check all lights (headlights, indicators, brake lights)",
    "Inspect windshield wipers",
    "Check battery terminals",
    "Inspect belts and hoses",
    "Road test after service"
)

@Singleton
class GarageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : GarageRepository {

    override suspend fun checkInVehicle(checkIn: VehicleCheckIn) {
        val currentUserId = auth.currentUser?.uid
            ?: throw IllegalStateException("No authenticated user")

        val docRef = firestore.collection("checkIns").document()

        val fullCheckIn = checkIn.copy(
            checkInId = docRef.id,
            checkedInByEmployeeId = currentUserId
        )

        docRef.set(fullCheckIn).await()

        val tasksCollection = firestore.collection("serviceTasks")
        DEFAULT_SERVICE_TASKS.forEach { description ->
            val taskRef = tasksCollection.document()
            val task = ServiceTask(
                taskId = taskRef.id,
                checkInId = docRef.id,
                taskDescription = description
            )
            taskRef.set(task).await()
        }
    }
}

