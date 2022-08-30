package com.codingtester.fitnote.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunData(run:Run)

    @Delete
    suspend fun deleteRun(run:Run)

    @Query("SELECT * FROM run_table order By timestamp Desc")
    fun getAllRunSortedByDate():LiveData<List<Run>>

    @Query("SELECT * FROM run_table order By timeInMillis Desc")
    fun getAllRunSortedByTimeInMillis():LiveData<List<Run>>

    @Query("SELECT * FROM run_table order By caloriesBurned Desc")
    fun getAllRunSortedByCalories():LiveData<List<Run>>

    @Query("SELECT * FROM run_table order By avgSpeedInKMH Desc")
    fun getAllRunSortedByAvgSpeed():LiveData<List<Run>>

    @Query("SELECT * FROM run_table order By distanceInMeters Desc")
    fun getAllRunSortedByDistance():LiveData<List<Run>>

    @Query("select sum(timeInMillis) from run_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("select sum(caloriesBurned) from run_table")
    fun getTotalCalories(): LiveData<List<Int>>

    @Query("select sum(distanceInMeters) from run_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("select AVG(avgSpeedInKMH) from run_table")
    fun getTotalAvgSpeed(): LiveData<Float>
}