package rs.nikolaivanovic.ssfitnessdnevniktreninga.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import rs.nikolaivanovic.ssfitnessdnevniktreninga.databinding.ItemSetBinding
import rs.nikolaivanovic.ssfitnessdnevniktreninga.models.SetEntry
import rs.nikolaivanovic.ssfitnessdnevniktreninga.R

class SetsAdapter(
    private val sets: MutableList<SetEntry>,
    private val onSetDelete: (Int) -> Unit
) : RecyclerView.Adapter<SetsAdapter.SetViewHolder>() {

    inner class SetViewHolder(private val binding: ItemSetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(set: SetEntry, position: Int) {
            binding.setNumber.text = binding.root.context.getString(
                R.string.set_number, position + 1
            )

            // --- REPS ---
            if (set.reps == 0) {
                if (!binding.repsEdit.isFocused) {
                    binding.repsEdit.setText("")
                    binding.repsEdit.hint = "0"
                }
            } else {
                binding.repsEdit.setText(set.reps.toString())
            }

            binding.repsEdit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (set.reps == 0) binding.repsEdit.setText("")
                } else {
                    val value = binding.repsEdit.text.toString()
                    set.reps = value.toIntOrNull() ?: 0
                    if (value.isEmpty()) {
                        binding.repsEdit.setText("")
                        binding.repsEdit.hint = "0"
                    } else {
                        binding.repsEdit.setText(set.reps.toString())
                    }
                }
            }

            // --- WEIGHT ---
            if (set.weight == 0f) {
                if (!binding.weightEdit.isFocused) {
                    binding.weightEdit.setText("")
                    binding.weightEdit.hint = "0.0"
                }
            } else {
                binding.weightEdit.setText(set.weight.toString())
            }

            binding.weightEdit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (set.weight == 0f) binding.weightEdit.setText("")
                } else {
                    val value = binding.weightEdit.text.toString()
                    set.weight = value.toFloatOrNull() ?: 0f
                    if (value.isEmpty()) {
                        binding.weightEdit.setText("")
                        binding.weightEdit.hint = "0.0"
                    } else {
                        binding.weightEdit.setText(set.weight.toString())
                    }
                }
            }

            // Spinner for unit selection
            val units = arrayOf("kg", "lbs")
            val context = binding.unitSpinner.context
            val adapter = ArrayAdapter(context, R.layout.spinner_selected_item, units)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.unitSpinner.adapter = adapter

            val unitIndex = units.indexOf(set.unit)
            binding.unitSpinner.setSelection(if (unitIndex >= 0) unitIndex else 0, false)

            binding.unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    val selectedUnit = units[pos]
                    if (set.unit != selectedUnit) {
                        set.unit = selectedUnit
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            binding.deleteSetButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onSetDelete(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemSetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position], position)
    }

    override fun getItemCount() = sets.size

    fun updateSets(newSets: List<SetEntry>) {
        sets.clear()
        sets.addAll(newSets)
        notifyDataSetChanged()
    }
}