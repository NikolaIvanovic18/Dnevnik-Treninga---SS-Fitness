package rs.nikolaivanovic.ssfitnessdnevniktreninga.data

import androidx.lifecycle.ViewModel
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.ExerciseEntry

class WorkoutViewModel(val repository: WorkoutRepository) : ViewModel() {
    var selectedDateMillis: Long? = null
    var workoutLoaded: Boolean = false
    val selectedMuscleGroups: MutableList<String> = mutableListOf()
    val exercises: MutableList<ExerciseEntry> = mutableListOf()
    var energyLevel: Int = 0
    var currentWorkoutId: Long = 0
}