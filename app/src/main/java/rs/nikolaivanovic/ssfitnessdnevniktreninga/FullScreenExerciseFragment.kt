package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters.SetsAdapter
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.FullScreenExerciseCardBinding
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.ExerciseEntry
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.SetEntry

class FullScreenExerciseFragment : Fragment() {
    private var _binding: FullScreenExerciseCardBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: ExerciseEntry
    private lateinit var setsAdapter: SetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercise = arguments?.getParcelable("exercise") ?: ExerciseEntry()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FullScreenExerciseCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.exerciseNameEdit.setText(exercise.name)
        binding.exerciseNameEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) exercise.name = binding.exerciseNameEdit.text.toString()
        }

        setsAdapter = SetsAdapter(exercise.sets) { setPos ->
            exercise.sets.removeAt(setPos)
            setsAdapter.notifyItemRemoved(setPos)
        }
        binding.setsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.setsRecyclerView.adapter = setsAdapter

        binding.addSetButton.setOnClickListener {
            if (exercise.sets.size < 5) {
                exercise.sets.add(SetEntry())
                setsAdapter.notifyItemInserted(exercise.sets.size - 1)
                binding.setsRecyclerView.scrollToPosition(exercise.sets.size - 1)
            } else {
                Toast.makeText(requireContext(), getString(R.string.max_sets_reached), Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveButton.setOnClickListener {
            binding.exerciseNameEdit.clearFocus()
            exercise.name = binding.exerciseNameEdit.text.toString()
            parentFragmentManager.setFragmentResult(
                "exercise_edit_result",
                bundleOf("exercise" to exercise, "deleted" to false, "saveWorkout" to true)
            )
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        binding.deleteExerciseButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                "exercise_edit_result",
                bundleOf("exercise" to exercise, "deleted" to true)
            )
            parentFragmentManager.popBackStack()
        }

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}