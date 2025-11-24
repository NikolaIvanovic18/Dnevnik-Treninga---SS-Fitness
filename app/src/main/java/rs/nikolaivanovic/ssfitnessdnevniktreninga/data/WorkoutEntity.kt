package rs.nikolaivanovic.ssfitnessdnevniktreninga.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val muscleGroups: String, // comma-separated
    val energyLevel: Int // 1-5 or similar
)