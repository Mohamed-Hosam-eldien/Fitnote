package com.codingtester.fitnote.data.local

import androidx.lifecycle.LiveData
import com.codingtester.fitnote.data.local.db.Run
import com.codingtester.fitnote.data.local.db.RunDao
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private var runDao: RunDao
    ): ILocalDataSource {

    override suspend fun insertRun(run: Run) {
        runDao.insertRunData(run)
    }

    override suspend fun deleteRun(run: Run) {
        runDao.deleteRun(run)
    }

    override fun getAllRunSortedByDate(): LiveData<List<Run>> {
        return runDao.getAllRunSortedByDate()
    }

    override fun getAllRunSortedByDistance(): LiveData<List<Run>> {
        return runDao.getAllRunSortedByDistance()
    }

    override fun getAllRunSortedByTimeInMillis(): LiveData<List<Run>> {
        return runDao.getAllRunSortedByTimeInMillis()
    }

    override fun getAllRunSortedByCalories(): LiveData<List<Run>> {
        return runDao.getAllRunSortedByCalories()
    }

    override fun getAllRunSortedByAvgSpeed(): LiveData<List<Run>> {
        return runDao.getAllRunSortedByAvgSpeed()
    }

    override fun getTotalAvgSpeed(): LiveData<Float> {
        return runDao.getTotalAvgSpeed()
    }

    override fun getTotalDistance(): LiveData<Int> {
        return runDao.getTotalDistance()
    }

    override fun getTotalCalories(): LiveData<Int> {
        return runDao.getTotalCalories()
    }

    override fun getTotalTimeInMillis(): LiveData<Long> {
        return runDao.getTotalTimeInMillis()
    }
}