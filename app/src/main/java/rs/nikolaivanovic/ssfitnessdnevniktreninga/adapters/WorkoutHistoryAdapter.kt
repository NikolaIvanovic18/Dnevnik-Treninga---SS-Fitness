package rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rs.nikolaivanovic.ssfitnessdnevniktreninga.R
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutEntity
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutRepository
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.ItemWorkoutHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryAdapter(
    private val onWorkoutClick: (WorkoutEntity) -> Unit,
    private val repository: WorkoutRepository
) : ListAdapter<WorkoutEntity, WorkoutHistoryAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = getItem(position)
        holder.bind(workout)
    }

    inner class WorkoutViewHolder(private val binding: ItemWorkoutHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: WorkoutEntity) {
            binding.root.setOnClickListener { onWorkoutClick(workout) }
            binding.detailsButton.setOnClickListener { onWorkoutClick(workout) }

            binding.historyDate.text = formatDate(binding.root.context, workout.date)
            binding.historyEnergy.text = binding.root.context.getString(
                R.string.energy,
                workout.energyLevel
            )

            // Localize muscle group names
            binding.historyMuscleGroups.text = workout.muscleGroups
                .split(",")
                .filter { it.isNotBlank() }
                .joinToString(", ") { getMuscleName(binding.root.context, it.trim()) }

            // Get exercise count through repository
            adapterScope.launch {
                val context = binding.root.context
                val exerciseCount = repository.getExerciseCountForWorkout(workout.id)

                withContext(Dispatchers.Main) {
                    binding.historyExerciseCount.text = getExerciseCountText(context, exerciseCount)
                }
            }
        }

        private fun formatDate(context: Context, dateMillis: Long): String {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val lang = prefs.getString("language", "en") ?: "en"
            val locale = if (lang == "sr") {
                Locale.Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build()
            } else {
                Locale.ENGLISH
            }
            val sdf = SimpleDateFormat("MMMM d, yyyy", locale)
            val formatted = sdf.format(Date(dateMillis))
            return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }

        private fun getMuscleName(context: Context, key: String): String {
            val resId = context.resources.getIdentifier(key, "string", context.packageName)
            return if (resId != 0) context.getString(resId) else key
        }

        private fun getExerciseCountText(context: Context, count: Int): String {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val lang = prefs.getString("language", "en") ?: "en"
            val prefix = " • "
            return if (lang == "sr") {
                val word = when {
                    count % 10 == 1 && count % 100 != 11 -> "vežba"
                    count % 10 in 2..4 && (count % 100 !in 12..14) -> "vežbe"
                    else -> "vežbi"
                }
                "$prefix$count $word"
            } else {
                val word = if (count == 1) "exercise" else "exercises"
                "$prefix$count $word"
            }
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<WorkoutEntity>() {
        override fun areItemsTheSame(oldItem: WorkoutEntity, newItem: WorkoutEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WorkoutEntity, newItem: WorkoutEntity): Boolean =
            oldItem == newItem
    }
}