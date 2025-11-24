package rs.nikolaivanovic.ssfitnessdnevniktreninga.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SetEntry(
    var reps: Int = 0,
    var weight: Float = 0f,
    var unit: String = "kg"
) : Parcelable

@Parcelize
data class ExerciseEntry(
    var id: Long = System.currentTimeMillis(),
    var name: String = "",
    var sets: MutableList<SetEntry> = mutableListOf(),
    var isExpanded: Boolean = false
) : Parcelable