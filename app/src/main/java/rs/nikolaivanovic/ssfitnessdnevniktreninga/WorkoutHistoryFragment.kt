package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters.WorkoutHistoryAdapter
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.AppDatabase
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.DatabaseProvider
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutEntity
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutRepository
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.FragmentWorkoutHistoryBinding

class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WorkoutHistoryAdapter
    private lateinit var repository: WorkoutRepository
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false)
        // Use DatabaseProvider instead of direct AppDatabase access to match WorkoutsFragment
        db = DatabaseProvider.getDatabase(requireContext())
        repository = WorkoutRepository(db)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadWorkouts()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter(
            { workout -> showWorkoutDetails(workout) },
            repository
        )

        binding.workoutHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.workoutHistoryRecyclerView.adapter = adapter

        // Add divider between items
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.recycler_divider)?.let {
            divider.setDrawable(it)
        }
        binding.workoutHistoryRecyclerView.addItemDecoration(divider)
    }

    override fun onResume() {
        super.onResume()
        // Reload the workout list every time the fragment becomes visible
        loadWorkouts()
    }

    private fun loadWorkouts() {
        lifecycleScope.launch {
            try {
                // Get workout list from repository
                val workouts = repository.getAllWorkoutsSortedByDateDesc()

                if (workouts.isEmpty()) {
                    binding.emptyHistoryText.visibility = View.VISIBLE
                    binding.workoutHistoryRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyHistoryText.visibility = View.GONE
                    binding.workoutHistoryRecyclerView.visibility = View.VISIBLE
                    adapter.submitList(workouts)
                }
            } catch (e: Exception) {
                Log.e("WorkoutHistory", "Error loading workouts", e)
                e.printStackTrace()
                binding.emptyHistoryText.visibility = View.VISIBLE
                binding.workoutHistoryRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun showWorkoutDetails(workout: WorkoutEntity) {
        val prefs = requireContext().getSharedPreferences("settings", 0)
        val unit = prefs.getString("unit", "kg") ?: "kg"

        WorkoutDetailsBottomSheet.newInstance(workout, unit)
            .show(parentFragmentManager, "workout_details")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}