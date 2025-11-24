package rs.nikolaivanovic.ssfitnessdnevniktreninga.data

import androidx.room.*

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId")
    suspend fun getExercisesForWorkout(workoutId: Long): List<ExerciseEntity>

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: Long)

    @Query("SELECT COUNT(*) FROM exercises WHERE workoutId = :workoutId")
    suspend fun getExerciseCountForWorkout(workoutId: Long): Int
}