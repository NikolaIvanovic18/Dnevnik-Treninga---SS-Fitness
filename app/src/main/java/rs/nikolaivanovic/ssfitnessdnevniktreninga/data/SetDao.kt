package rs.nikolaivanovic.ssfitnessdnevniktreninga.data

import androidx.room.*

@Dao
interface SetDao {
    @Insert
    suspend fun insertSet(set: SetEntity): Long

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    suspend fun getSetsForExercise(exerciseId: Long): List<SetEntity>

    @Delete
    suspend fun deleteSet(set: SetEntity)

    @Query("DELETE FROM sets WHERE exerciseId = :exerciseId")
    suspend fun deleteSetsForExercise(exerciseId: Long)
}