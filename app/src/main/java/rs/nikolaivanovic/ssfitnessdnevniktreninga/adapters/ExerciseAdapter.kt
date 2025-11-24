package rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import rs.nikolaivanovic.ssfitnessdnevniktreninga.R
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.ItemExerciseCardBinding
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.ExerciseEntry
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.SetEntry

class ExerciseAdapter(
    private val exercises: MutableList<ExerciseEntry>,
    private val onDelete: (Int) -> Unit,
    private val onExpand: (Int) -> Unit,
    private val onExerciseChanged: ((ExerciseEntry) -> Unit)? = null
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(val binding: ItemExerciseCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var nameWatcher: TextWatcher? = null

        fun bind(exercise: ExerciseEntry) {
            // Remove previous text change listener if exists
            nameWatcher?.let { binding.exerciseNameEdit.removeTextChangedListener(it) }

            // Show the current name or empty
            binding.exerciseNameEdit.setText(exercise.name)
            binding.exerciseNameEdit.hint = binding.root.context.getString(R.string.exercise_name_hint)

            // Create new TextWatcher to track name changes
            nameWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newName = s?.toString() ?: ""
                    if (exercise.name != newName) {
                        exercise.name = newName
                        onExerciseChanged?.invoke(exercise)
                    }
                }
            }
            binding.exerciseNameEdit.addTextChangedListener(nameWatcher)

            binding.contentLayout.visibility = View.GONE
            binding.expandButton.rotation = 0f

            val openFullScreen = View.OnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onExpand(pos)
                }
            }
            binding.headerLayout.setOnClickListener(openFullScreen)
            binding.expandButton.setOnClickListener(openFullScreen)
            binding.headerLayout.setOnLongClickListener { openFullScreen.onClick(it); true }
            binding.expandButton.setOnLongClickListener { openFullScreen.onClick(it); true }

            val setsAdapter = SetsAdapter(exercise.sets) { setPos ->
                exercise.sets.removeAt(setPos)
                notifyItemChanged(adapterPosition)
                onExerciseChanged?.invoke(exercise)
            }
            binding.setsRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.setsRecyclerView.adapter = setsAdapter

            binding.addSetButton.setOnClickListener {
                if (exercise.sets.size < 5) {
                    exercise.sets.add(SetEntry())
                    notifyItemChanged(adapterPosition)
                    onExerciseChanged?.invoke(exercise)
                } else {
                    Toast.makeText(binding.root.context, R.string.max_sets_reached, Toast.LENGTH_SHORT).show()
                }
            }

            binding.deleteExerciseButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount() = exercises.size
}