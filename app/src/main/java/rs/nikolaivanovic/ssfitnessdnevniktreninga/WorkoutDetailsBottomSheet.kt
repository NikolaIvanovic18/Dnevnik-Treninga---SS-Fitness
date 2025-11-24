package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.DatabaseProvider
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutEntity
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutRepository
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.BottomSheetWorkoutDetailsBinding
import java.text.SimpleDateFormat
import java.util.*

class WorkoutDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWorkoutDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: WorkoutRepository

    companion object {
        private const val ARG_WORKOUT_ID = "arg_workout_id"
        private const val ARG_UNIT = "arg_unit"

        fun newInstance(workout: WorkoutEntity, unit: String): WorkoutDetailsBottomSheet {
            val fragment = WorkoutDetailsBottomSheet()
            val args = Bundle()
            args.putLong(ARG_WORKOUT_ID, workout.id)
            args.putString(ARG_UNIT, unit)
            fragment.arguments = args
            return fragment
        }
    }

    private fun getAppLanguage(context: android.content.Context): String {
        val prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    private fun formatDate(context: android.content.Context, dateMillis: Long): String {
        val lang = getAppLanguage(context)
        val locale = if (lang == "sr") {
            Locale.Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build()
        } else {
            Locale.ENGLISH
        }
        val sdf = SimpleDateFormat("MMMM d, yyyy", locale)
        val formatted = sdf.format(Date(dateMillis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    private fun getMuscleName(context: android.content.Context, key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWorkoutDetailsBinding.inflate(inflater, container, false)
        // Use DatabaseProvider instead of direct AppDatabase access
        val db = DatabaseProvider.getDatabase(requireContext())
        repository = WorkoutRepository(db)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workoutId = arguments?.getLong(ARG_WORKOUT_ID) ?: return
        val unit = arguments?.getString(ARG_UNIT) ?: "kg"

        loadWorkoutDetails(workoutId, unit)
    }

    private fun loadWorkoutDetails(workoutId: Long, unit: String) {
        lifecycleScope.launch {
            try {
                Log.d("WorkoutDetails", "Loading workout with ID: $workoutId")

                // Get workout directly by ID using repository
                val workout = repository.getWorkoutById(workoutId)

                if (workout == null) {
                    Log.e("WorkoutDetails", "Workout with ID $workoutId not found")
                    binding.detailsExercises.text = getString(R.string.no_workouts_yet)
                    return@launch
                }

                Log.d("WorkoutDetails", "Found workout: ${workout.id}, date=${workout.date}")

                // Format and display the date
                binding.detailsDate.text = formatDate(requireContext(), workout.date)

                // Process and display muscle groups with localized names
                binding.detailsMuscleGroups.text = workout.muscleGroups
                    .split(",")
                    .filter { it.isNotBlank() }
                    .joinToString(", ") { getMuscleName(requireContext(), it.trim()) }

                // Display energy level
                binding.detailsEnergy.text = getString(R.string.energy, workout.energyLevel)

                // Get exercises for this workout using repository
                val exercises = repository.getExercisesForWorkout(workoutId)
                Log.d("WorkoutDetails", "Found ${exercises.size} exercises")

                if (exercises.isEmpty()) {
                    binding.detailsExercises.text = getString(R.string.no_exercises_recorded)
                } else {
                    val exerciseDetails = StringBuilder()

                    for (exercise in exercises) {
                        exerciseDetails.append("- ${exercise.name}\n")

                        val sets = repository.getSetsForExercise(exercise.id)
                        for ((index, set) in sets.withIndex()) {
                            // Display sets with their original units (no conversion needed)
                            exerciseDetails.append(
                                "    ${getString(R.string.set_number, index + 1)} " +
                                        "${set.reps} ${getString(R.string.reps)}, " +
                                        "${set.weight} ${set.unit}\n"
                            )
                        }

                        if (exercise != exercises.last()) {
                            exerciseDetails.append("\n")
                        }
                    }

                    binding.detailsExercises.text = exerciseDetails.toString()
                }

            } catch (e: Exception) {
                Log.e("WorkoutDetails", "Error loading workout details", e)
                binding.detailsExercises.text = getString(R.string.could_not_load_exercises)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}