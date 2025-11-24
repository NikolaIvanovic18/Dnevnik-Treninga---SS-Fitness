package rs.nikolaivanovic.ssfitnessdnevniktreninga.data

import androidx.room.*

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE date = :dateMillis")
    suspend fun deleteWorkoutByDate(dateMillis: Long)

    @Query("SELECT * FROM workouts WHERE date = :dateMillis LIMIT 1")
    suspend fun getWorkoutByDate(dateMillis: Long): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    suspend fun getAllWorkoutsSortedByDateDesc(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?
}