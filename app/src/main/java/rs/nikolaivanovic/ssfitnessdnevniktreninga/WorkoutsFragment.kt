package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters.ExerciseAdapter
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.*
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.FragmentWorkoutsBinding
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.ExerciseEntry
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.SetEntry
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.size
import androidx.core.view.get

class WorkoutsFragment : Fragment() {
    private var _binding: FragmentWorkoutsBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter

    private val muscleGroupKeys = listOf(
        "muscle_back", "muscle_shoulders", "muscle_biceps", "muscle_triceps",
        "muscle_chest", "muscle_quads", "muscle_hamstrings", "muscle_calves",
        "muscle_abs", "muscle_cardio"
    )

    private val viewModel: WorkoutViewModel by activityViewModels {
        WorkoutViewModelFactory(
            WorkoutRepository(DatabaseProvider.getDatabase(requireContext()))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControlsEnabled(viewModel.selectedDateMillis != null)
        setupDatePicker()
        setupDeleteButton()
        setupMuscleGroupButton()
        setupExerciseList()
        setupSaveButton()
        restoreStateToUI()
    }

    private fun setControlsEnabled(enabled: Boolean) {
        val visibilityValue = if (enabled) View.VISIBLE else View.INVISIBLE

        binding.addExerciseButton.isEnabled = enabled
        binding.addExerciseButton.visibility = visibilityValue

        binding.deleteWorkoutButton.isEnabled = enabled
        binding.deleteWorkoutButton.visibility = if (enabled && viewModel.workoutLoaded) View.VISIBLE else View.GONE

        binding.saveWorkoutButton.isEnabled = enabled
        binding.saveWorkoutButton.visibility = visibilityValue

        binding.muscleGroupButton.isEnabled = enabled
        binding.muscleGroupButton.visibility = visibilityValue

        binding.energyLabel.visibility = visibilityValue
        binding.energyRadioGroup.isEnabled = enabled
        binding.energyRadioGroup.visibility = visibilityValue

        // Fix the reassignment issue by not using apply block
        for (i in 0 until binding.energyRadioGroup.childCount) {
            val child = binding.energyRadioGroup.getChildAt(i)
            child.isEnabled = enabled
            child.visibility = visibilityValue
        }

        binding.exercisesRecyclerView.isEnabled = enabled
        binding.exercisesRecyclerView.visibility = visibilityValue
    }

    private fun formatDateForButton(dateMillis: Long): String {
        val locale = Locale.getDefault()
        val sdf = SimpleDateFormat("MMMM d, yyyy", locale)
        val formatted = sdf.format(Date(dateMillis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    private fun setupDatePicker() {
        binding.datePickerButton.setOnClickListener {
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .setSelection(viewModel.selectedDateMillis ?: System.currentTimeMillis())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                viewModel.selectedDateMillis = selection
                binding.datePickerButton.text = formatDateForButton(selection)
                setControlsEnabled(true)
                // Launch coroutine to call the suspend function
                lifecycleScope.launch {
                    loadWorkoutForDate(selection)
                }
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupDeleteButton() {
        binding.deleteWorkoutButton.setOnClickListener {
            if (viewModel.selectedDateMillis != null && viewModel.workoutLoaded) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_workout))
                    .setMessage(getString(R.string.delete_workout_confirm))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        deleteWorkoutForDate(viewModel.selectedDateMillis!!)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }
    }

    private fun getMuscleName(key: String): String {
        val resId = resources.getIdentifier(key, "string", requireContext().packageName)
        return if (resId != 0) getString(resId) else key
    }

    private fun setupMuscleGroupButton() {
        binding.muscleGroupButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.muscleGroupButton, Gravity.NO_GRAVITY, 0, R.style.AppPopupMenu)

            // Add muscle group items to popup menu
            for ((index, key) in muscleGroupKeys.withIndex()) {
                val name = getMuscleName(key)
                val item = popup.menu.add(Menu.NONE, index, Menu.NONE, name)
                item.isCheckable = true
                item.isChecked = viewModel.selectedMuscleGroups.contains(key)
            }

            popup.menu.setGroupCheckable(Menu.NONE, true, false)
            popup.setOnMenuItemClickListener { item ->
                val key = muscleGroupKeys.getOrNull(item.itemId)
                key?.let {
                    if (viewModel.selectedMuscleGroups.contains(it)) {
                        viewModel.selectedMuscleGroups.remove(it)
                        item.isChecked = false
                    } else {
                        viewModel.selectedMuscleGroups.add(it)
                        item.isChecked = true
                    }
                    updateMuscleGroupButtonText()
                }
                false
            }

            // Apply color to menu items
            val color = ContextCompat.getColor(requireContext(), R.color.black)
            for (i in 0 until popup.menu.size) {
                val menuItem = popup.menu[i]
                val spanString = SpannableString(menuItem.title)
                spanString.setSpan(ForegroundColorSpan(color), 0, spanString.length, 0)
                menuItem.title = spanString
            }

            popup.show()
        }
        updateMuscleGroupButtonText()
    }

    private fun updateMuscleGroupButtonText() {
        binding.muscleGroupButton.text = if (viewModel.selectedMuscleGroups.isEmpty()) {
            getString(R.string.muscle_group)
        } else {
            viewModel.selectedMuscleGroups.joinToString(", ") { getMuscleName(it) }
        }
    }

    private fun setupExerciseList() {
        exerciseAdapter = ExerciseAdapter(
            viewModel.exercises,
            onDelete = { position ->
                viewModel.exercises.removeAt(position)
                exerciseAdapter.notifyItemRemoved(position)
            },
            onExpand = { position ->
                val fragment = FullScreenExerciseFragment()
                val args = Bundle()
                args.putParcelable("exercise", viewModel.exercises[position])
                fragment.arguments = args
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onExerciseChanged = { exercise ->
                // This triggers auto-save if needed in the future
                // The exercise is already in viewModel.exercises
            }
        )

        binding.exercisesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
        }

        binding.addExerciseButton.setOnClickListener {
            val newExercise = ExerciseEntry(
                id = System.currentTimeMillis(),
                name = getString(R.string.new_exercise),
                isExpanded = false
            )
            viewModel.exercises.add(newExercise)
            exerciseAdapter.notifyItemInserted(viewModel.exercises.size - 1)
        }

        parentFragmentManager.setFragmentResultListener("exercise_edit_result", viewLifecycleOwner) { _, bundle ->
            @Suppress("Deprecation")
            val updatedExercise = bundle.getParcelable<ExerciseEntry>("exercise")
            val deleted = bundle.getBoolean("deleted", false)
            val saveWorkoutNow = bundle.getBoolean("saveWorkout", false)

            updatedExercise?.let { exercise ->
                val pos = viewModel.exercises.indexOfFirst { it.id == exercise.id }
                if (pos != -1) {
                    if (deleted) {
                        viewModel.exercises.removeAt(pos)
                        exerciseAdapter.notifyItemRemoved(pos)
                    } else {
                        viewModel.exercises[pos] = exercise.copy(isExpanded = false)
                        exerciseAdapter.notifyItemChanged(pos)
                        if (saveWorkoutNow) {
                            // Get current energy level from radio buttons before saving
                            val checkedRadioId = binding.energyRadioGroup.checkedRadioButtonId
                            viewModel.energyLevel = when (checkedRadioId) {
                                binding.energy1.id -> 1
                                binding.energy2.id -> 2
                                binding.energy3.id -> 3
                                binding.energy4.id -> 4
                                binding.energy5.id -> 5
                                binding.energy6.id -> 6
                                binding.energy7.id -> 7
                                else -> 0
                            }

                            // Launch coroutine to call the suspend function
                            lifecycleScope.launch {
                                saveWorkout()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveWorkoutButton.setOnClickListener {
            val dateMillis = viewModel.selectedDateMillis
            if (dateMillis == null) {
                Toast.makeText(requireContext(), getString(R.string.please_select_date), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.selectedMuscleGroups.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_select_muscle_groups), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.exercises.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_add_exercise), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get energy level from radio buttons
            val checkedRadioId = binding.energyRadioGroup.checkedRadioButtonId
            viewModel.energyLevel = when (checkedRadioId) {
                binding.energy1.id -> 1
                binding.energy2.id -> 2
                binding.energy3.id -> 3
                binding.energy4.id -> 4
                binding.energy5.id -> 5
                binding.energy6.id -> 6
                binding.energy7.id -> 7
                else -> 0
            }

            // Use the common saveWorkout method
            lifecycleScope.launch {
                saveWorkout()
            }
        }
    }

    private suspend fun loadWorkoutForDate(dateMillis: Long) {
        val workout = viewModel.repository.getWorkoutByDate(dateMillis)

        if (workout != null) {
            // Store workout id for updating later
            viewModel.currentWorkoutId = workout.id

            // Load muscle groups
            viewModel.selectedMuscleGroups.clear()
            viewModel.selectedMuscleGroups.addAll(workout.muscleGroups.split(",").filter { it.isNotBlank() })
            updateMuscleGroupButtonText()

            // Load exercises
            val exerciseEntities = viewModel.repository.getExercisesForWorkout(workout.id)
            val exercises = exerciseEntities.map { exerciseEntity ->
                val setEntities = viewModel.repository.getSetsForExercise(exerciseEntity.id)
                val sets = setEntities.map { setEntity ->
                    SetEntry(
                        reps = setEntity.reps,
                        weight = setEntity.weight,
                        unit = setEntity.unit
                    )
                }.toMutableList()

                ExerciseEntry(
                    id = exerciseEntity.id,
                    name = exerciseEntity.name,
                    sets = sets,
                    isExpanded = false
                )
            }

            viewModel.exercises.clear()
            viewModel.exercises.addAll(exercises)
            exerciseAdapter.notifyDataSetChanged()

            // Restore energy level
            viewModel.energyLevel = workout.energyLevel
            updateEnergyRadioButtons(workout.energyLevel)

            viewModel.workoutLoaded = true
            binding.deleteWorkoutButton.visibility = View.VISIBLE
            updateDatePickerHighlight(true)
        } else {
            // No workout found
            viewModel.currentWorkoutId = 0
            viewModel.selectedMuscleGroups.clear()
            updateMuscleGroupButtonText()
            viewModel.exercises.clear()
            exerciseAdapter.notifyDataSetChanged()
            viewModel.energyLevel = 0
            updateEnergyRadioButtons(0)
            viewModel.workoutLoaded = false
            binding.deleteWorkoutButton.visibility = View.GONE
            updateDatePickerHighlight(false)
        }
    }

    // Helper method to update the energy radio buttons
    private fun updateEnergyRadioButtons(energyLevel: Int) {
        val radioId = when (energyLevel) {
            1 -> R.id.energy1
            2 -> R.id.energy2
            3 -> R.id.energy3
            4 -> R.id.energy4
            5 -> R.id.energy5
            6 -> R.id.energy6
            7 -> R.id.energy7
            else -> -1
        }

        if (radioId != -1) {
            binding.energyRadioGroup.check(radioId)
        } else {
            binding.energyRadioGroup.clearCheck()
        }
    }

    private fun deleteWorkoutForDate(dateMillis: Long) {
        lifecycleScope.launch {
            viewModel.repository.deleteWorkoutByDate(dateMillis)

            Toast.makeText(requireContext(), getString(R.string.workout_deleted), Toast.LENGTH_SHORT).show()

            viewModel.currentWorkoutId = 0
            viewModel.selectedMuscleGroups.clear()
            updateMuscleGroupButtonText()

            viewModel.exercises.clear()
            exerciseAdapter.notifyDataSetChanged()

            binding.energyRadioGroup.post {
                binding.energyRadioGroup.clearCheck()
            }

            viewModel.energyLevel = 0
            viewModel.workoutLoaded = false
            binding.deleteWorkoutButton.visibility = View.GONE
            updateDatePickerHighlight(false)
        }
    }

    private fun updateDatePickerHighlight(isHighlighted: Boolean) {
        if (isHighlighted) {
            binding.datePickerButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.highlight)
            )
            binding.datePickerButton.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
        } else {
            binding.datePickerButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            binding.datePickerButton.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }

    private fun restoreStateToUI() {
        // Restore date
        viewModel.selectedDateMillis?.let {
            binding.datePickerButton.text = formatDateForButton(it)
            setControlsEnabled(true)
        }

        // Restore muscle groups
        updateMuscleGroupButtonText()

        // Restore exercises if adapter is initialized
        if (::exerciseAdapter.isInitialized) {
            exerciseAdapter.notifyDataSetChanged()
        }

        // Restore energy level using the helper method
        updateEnergyRadioButtons(viewModel.energyLevel)

        // Restore delete button visibility
        binding.deleteWorkoutButton.visibility = if (viewModel.workoutLoaded) View.VISIBLE else View.GONE
        updateDatePickerHighlight(viewModel.workoutLoaded)
    }

    private suspend fun saveWorkout() {
        val dateMillis = viewModel.selectedDateMillis ?: return
        val muscleGroupsStr = viewModel.selectedMuscleGroups.joinToString(",")
        val energyLevel = viewModel.energyLevel

        try {
            // Create workout entity using currentWorkoutId
            val workout = WorkoutEntity(
                id = viewModel.currentWorkoutId, // This will be 0 for new workouts
                date = dateMillis,
                muscleGroups = muscleGroupsStr,
                energyLevel = energyLevel
            )

            // Insert/update workout and get the ID
            val workoutId = viewModel.repository.insertWorkout(workout)
            viewModel.currentWorkoutId = workoutId // Update for future saves

            // Delete existing exercises for this workout
            if (workoutId > 0) {
                viewModel.repository.deleteExercisesForWorkout(workoutId)
            }

            // Save all exercises and their sets
            for (exercise in viewModel.exercises) {
                val exerciseEntity = ExerciseEntity(
                    workoutId = workoutId,
                    name = exercise.name
                )
                val exerciseId = viewModel.repository.insertExercise(exerciseEntity)

                for (set in exercise.sets) {
                    val setEntity = SetEntity(
                        exerciseId = exerciseId,
                        reps = set.reps,
                        weight = set.weight,
                        unit = set.unit
                    )
                    viewModel.repository.insertSet(setEntity)
                }
            }

            Toast.makeText(requireContext(), getString(R.string.workout_saved), Toast.LENGTH_SHORT).show()
            viewModel.workoutLoaded = true
            binding.deleteWorkoutButton.visibility = View.VISIBLE
            updateDatePickerHighlight(true)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving workout: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}