package rs.nikolaivanovic.ssfitnessdnevniktreninga.data

class WorkoutRepository(private val db: AppDatabase) {
    suspend fun getAllWorkouts(): List<WorkoutEntity> =
        db.workoutDao().getAllWorkouts()

    suspend fun deleteWorkout(workout: WorkoutEntity) =
        db.workoutDao().deleteWorkout(workout)

    suspend fun insertExercise(exercise: ExerciseEntity): Long =
        db.exerciseDao().insertExercise(exercise)

    suspend fun getExercisesForWorkout(workoutId: Long): List<ExerciseEntity> =
        db.exerciseDao().getExercisesForWorkout(workoutId)

    suspend fun deleteExercise(exercise: ExerciseEntity) =
        db.exerciseDao().deleteExercise(exercise)

    suspend fun insertSet(set: SetEntity): Long =
        db.setDao().insertSet(set)

    suspend fun getSetsForExercise(exerciseId: Long): List<SetEntity> =
        db.setDao().getSetsForExercise(exerciseId)

    suspend fun deleteSet(set: SetEntity) =
        db.setDao().deleteSet(set)

    suspend fun deleteWorkoutByDate(dateMillis: Long) {
        db.workoutDao().deleteWorkoutByDate(dateMillis)
    }

    suspend fun getWorkoutByDate(dateMillis: Long): WorkoutEntity? {
        return db.workoutDao().getWorkoutByDate(dateMillis)
    }

    suspend fun deleteSetsForExercise(exerciseId: Long) {
        db.setDao().deleteSetsForExercise(exerciseId)
    }

    suspend fun deleteExercisesForWorkout(workoutId: Long) {
        db.exerciseDao().deleteExercisesForWorkout(workoutId)
    }

    suspend fun insertWorkout(workout: WorkoutEntity): Long {
        return db.workoutDao().insertWorkout(workout)
    }

    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? {
        return db.workoutDao().getWorkoutById(workoutId)
    }

    suspend fun getAllWorkoutsSortedByDateDesc(): List<WorkoutEntity> =
        db.workoutDao().getAllWorkoutsSortedByDateDesc()

    suspend fun getExerciseCountForWorkout(workoutId: Long): Int =
        db.exerciseDao().getExerciseCountForWorkout(workoutId)
}