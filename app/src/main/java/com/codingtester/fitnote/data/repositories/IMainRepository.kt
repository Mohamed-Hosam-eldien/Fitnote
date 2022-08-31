package com.codingtester.fitnote.data.repositories

import androidx.lifecycle.LiveData
import com.codingtester.fitnote.data.local.db.Run

interface IMainRepository {

    suspend fun insertRun(run: Run)

    suspend fun deleteRun(run: Run)

    fun getAllRunSortedByDate(): LiveData<List<Run>>

    fun getAllRunSortedByDistance(): LiveData<List<Run>>

    fun getAllRunSortedByTimeInMillis(): LiveData<List<Run>>

    fun getAllRunSortedByCalories(): LiveData<List<Run>>

    fun getAllRunSortedByAvgSpeed(): LiveData<List<Run>>

    fun getTotalAvgSpeed(): LiveData<Float>

    fun getTotalDistance(): LiveData<Int>

    fun getTotalCalories(): LiveData<Int>

    fun getTotalTimeInMillis(): LiveData<Long>

}