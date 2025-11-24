package rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rs.nikolaivanovic.ssfitnessdnevniktreninga.R
import rs.nikolaivanovic.ssfitnessdnevniktreninga.data.WorkoutEntity
import java.text.SimpleDateFormat
import java.util.*

class WorkoutsAdapter(private var workouts: List<WorkoutEntity>) :
    RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {

    fun updateWorkouts(newWorkouts: List<WorkoutEntity>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_card, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount() = workouts.size

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.datePickerButton)
        private val muscleGroupsText: TextView = itemView.findViewById(R.id.muscleGroupButton)

        fun bind(workout: WorkoutEntity) {
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            dateText.text = sdf.format(Date(workout.date))
            muscleGroupsText.text = workout.muscleGroups
        }
    }
}