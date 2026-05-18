package com.valentinegarage.data



import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// This class handles ALL communication with Firebase database
// Think of it as the "messenger" between your app and the database
class GarageRepository {

    // This connects us to the Firebase Firestore database
    private val firestore = FirebaseFirestore.getInstance()

    // This function saves the check-in form to Firebase
    // "suspend" means it runs in the background without freezing the app
    suspend fun checkInVehicle(checkIn: VehicleCheckIn): String {

        // Create a new empty document in the "checkIns" collection
        // Firebase automatically gives it a unique ID
        val docRef = firestore.collection("checkIns").document()

        // Copy the check-in data and add the generated ID
        val checkInWithId = checkIn.copy(checkInId = docRef.id)

        // Save to Firebase and wait until it's done
        docRef.set(checkInWithId).await()

        // Return the ID so we know which truck was checked in
        return docRef.id
    }
}