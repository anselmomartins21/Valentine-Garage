package com.example.vgarage  

import android.util.Log
import androidx.compose.runtime.Composable
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

// ── Data classes ──────────────────────────────────────────────────────────────

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val pin: String = "",
    @SerialName("created_at") val createdAt: String? = null   // present in DB; ignore safely
)

@Serializable
data class Truck(
    val id: String = "",
    @SerialName("plate_number")     val plateNumber: String = "",
    val condition: String = "",
    val kilometers: Int = 0,
    // These three columns are NULL in the DB when TruckInsert does not set them.
    // Declaring them as String? prevents kotlinx.serialization from throwing
    // "Expected string but got null" which would silently return emptyList().
    @SerialName("check_in_date")    val checkInDate: String? = null,
    @SerialName("checked_by")       val checkedBy: String? = null,
    val repairs: String = "",
    @SerialName("additional_notes") val additionalNotes: String = "",
    @SerialName("owner_name")       val ownerName: String? = null,
    val status: String? = null
)

@Serializable
data class Task(
    val id: String = "",
    @SerialName("truck_id")         val truckId: String = "",
    @SerialName("mechanic_id")      val mechanicId: String = "",
    @SerialName("task_description") val description: String = "",
    val status: String = "pending",
    // notes can be NULL in the DB for newly created (pending) tasks
    val notes: String? = null,
    @SerialName("completed_at")     val completedAt: String? = null,
    @SerialName("created_at")       val createdAt: String? = null
)

data class FleetStats(
    val total: Int,
    val inRepair: Int,
    val completed: Int,
    val needsAttention: Int
)

// ── Session ───────────────────────────────────────────────────────────────────

object CurrentUser {
    var id: String   = ""
    var name: String = ""
    var role: String = ""
    private var lastActivity: Long = 0L

    fun recordActivity() { lastActivity = System.currentTimeMillis() }

    fun isExpired(): Boolean {
        if (id.isEmpty()) return false
        val fiveMinutes = 5 * 60 * 1000L
        return System.currentTimeMillis() - lastActivity > fiveMinutes
    }

    fun clear() { id = ""; name = ""; role = ""; lastActivity = 0L }
}

// ── Private insert models (typed, not Map<String,Any>) ───────────────────────

@Serializable
private data class TruckInsert(
    val id: String,
    @SerialName("plate_number")     val plateNumber: String,
    val condition: String,
    val kilometers: Int,
    @SerialName("checked_by")       val checkedBy: String,
    val repairs: String,
    @SerialName("additional_notes") val additionalNotes: String
)

@Serializable
private data class TaskInsert(
    val id: String,
    @SerialName("truck_id")         val truckId: String,
    @SerialName("mechanic_id")      val mechanicId: String,
    @SerialName("task_description") val description: String,
    val status: String = "pending",
    val notes: String = ""
)

// ── Repository ────────────────────────────────────────────────────────────────

object GarageRepository {
    private val db = SupabaseClient.client
    private const val TAG = "VGARAGE"

    // Default fallback tasks when none are explicitly selected at check-in
    private val DEFAULT_TASKS = listOf(
        "Oil Change", "Brake Inspection", "Tyre Check",
        "Fluid Levels", "Engine Diagnostics"
    )

    suspend fun login(email: String, pin: String): User? = try {
        db.from("Users").select {
            filter {
                eq("email", email.lowercase().trim())
                eq("pin", pin.trim())
            }
        }.decodeSingleOrNull<User>()
    } catch (e: Exception) {
        Log.e(TAG, "Login failed: ${e.message}")
        null
    }

    /**
     * Check in a truck and create task records from the repairs selected during check-in.
     * @param taskList The repairs ticked on the check-in form; if empty the default list is used.
     */
    suspend fun checkInTruck(
        plate: String,
        km: Int,
        condition: String,
        repairs: String,
        additionalNotes: String,
        userId: String,
        taskList: List<String> = DEFAULT_TASKS
    ): String {
        val truckId = UUID.randomUUID().toString()
        val tasksToCreate = if (taskList.isEmpty()) DEFAULT_TASKS else taskList

        // 1. Insert the truck record
        Log.d(TAG, "Inserting truck: plate=$plate km=$km condition=$condition")
        db.from("trucks").insert(
            TruckInsert(
                id              = truckId,
                plateNumber     = plate,
                condition       = condition,
                kilometers      = km,
                checkedBy       = userId,
                repairs         = repairs,
                additionalNotes = additionalNotes
            )
        )
        Log.d(TAG, "Truck inserted: $truckId")

        // 2. Insert one task per selected repair
        var successCount = 0
        tasksToCreate.forEach { desc ->
            try {
                db.from("tasks").insert(
                    TaskInsert(
                        id          = UUID.randomUUID().toString(),
                        truckId     = truckId,
                        mechanicId  = userId,
                        description = desc
                    )
                )
                successCount++
            } catch (e: Exception) {
                Log.e(TAG, "Task '$desc' failed: ${e.message}")
            }
        }
        Log.d(TAG, "$successCount/${tasksToCreate.size} tasks created for $truckId")
        return truckId
    }

    suspend fun getTasksForTruck(truckId: String): List<Task> = try {
        Log.d(TAG, "Loading tasks for truck: $truckId")
        val result = db.from("tasks").select {
            filter { eq("truck_id", truckId) }
        }.decodeList<Task>()
        Log.d(TAG, "Found ${result.size} tasks")
        result
    } catch (e: Exception) {
        Log.e(TAG, "Tasks load failed: ${e.message}")
        emptyList()
    }

    suspend fun completeTask(taskId: String, notes: String) {
        try {
            db.from("tasks").update({
                set("status", "completed")
                set("notes", notes)
            }) { filter { eq("id", taskId) } }
            Log.d(TAG, "Task $taskId completed")
        } catch (e: Exception) {
            Log.e(TAG, "Complete task failed: ${e.message}")
            throw e
        }
    }

    /**
     * Returns the most recent truck with [plate] that still has at least one pending task,
     * or null if no active (in-service) truck exists for that plate.
     * Used by CheckInActivity to detect duplicate check-ins.
     */
    suspend fun getActiveTruckByPlate(plate: String): Truck? {
        return try {
            val trucks = db.from("trucks").select {
                filter { eq("plate_number", plate.trim().uppercase()) }
            }.decodeList<Truck>()
            if (trucks.isEmpty()) return null
            val tasks = getAllTasks()
            // Return the first truck that still has at least one pending task
            trucks.firstOrNull { truck ->
                tasks.any { it.truckId == truck.id && it.status == "pending" }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getActiveTruckByPlate failed: ${e.message}")
            null
        }
    }

    suspend fun getAllTrucks(): List<Truck> = try {
        db.from("trucks").select().decodeList<Truck>()
    } catch (e: Exception) {
        Log.e(TAG, "getAllTrucks failed: ${e.message}")
        emptyList()
    }

    suspend fun getAllTasks(): List<Task> = try {
        db.from("tasks").select().decodeList<Task>()
    } catch (e: Exception) {
        Log.e(TAG, "getAllTasks failed: ${e.message}")
        emptyList()
    }

    suspend fun getAllUsers(): List<User> = try {
        db.from("Users").select().decodeList<User>()
    } catch (e: Exception) {
        Log.e(TAG, "getAllUsers failed: ${e.message}")
        emptyList()
    }

    suspend fun getFleetStats(): FleetStats {
        return try {
            val trucks = getAllTrucks()
            val tasks  = getAllTasks()
            Log.d(TAG, "Stats: ${trucks.size} trucks, ${tasks.size} tasks")
            FleetStats(
                total = trucks.size,
                inRepair = trucks.count { truck ->
                    tasks.filter { it.truckId == truck.id }.any { it.status == "pending" }
                },
                completed = trucks.count { truck ->
                    val tt = tasks.filter { it.truckId == truck.id }
                    tt.isNotEmpty() && tt.all { it.status == "completed" }
                },
                needsAttention = trucks.count {
                    it.condition == "Poor" || it.condition == "Critical"
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "getFleetStats failed: ${e.message}")
            FleetStats(0, 0, 0, 0)
        }
    }

    @Composable
    fun AutoLogoutHandler(onTimeout: () -> Unit) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000L)
                if (CurrentUser.isExpired()) {
                    CurrentUser.clear()
                    onTimeout()
                    break
                }
            }
        }
    }
}
