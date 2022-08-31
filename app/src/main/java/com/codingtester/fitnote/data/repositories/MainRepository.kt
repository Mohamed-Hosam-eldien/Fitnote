package com.codingtester.fitnote.data.repositories

import androidx.lifecycle.LiveData
import com.codingtester.fitnote.data.local.ILocalDataSource
import com.codingtester.fitnote.data.local.db.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    private var localDataSource: ILocalDataSource
    ): IMainRepository {

    override suspend fun insertRun(run: Run) {
        localDataSource.insertRun(run)
    }

    override suspend fun deleteRun(run: Run) {
        localDataSource.deleteRun(run)
    }

    override fun getAllRunSortedByDate(): LiveData<List<Run>> {
        return localDataSource.getAllRunSortedByDate()
    }

    override fun getAllRunSortedByDistance(): LiveData<List<Run>> {
        return localDataSource.getAllRunSortedByDistance()
    }

    override fun getAllRunSortedByTimeInMillis(): LiveData<List<Run>> {
        return localDataSource.getAllRunSortedByTimeInMillis()
    }

    override fun getAllRunSortedByCalories(): LiveData<List<Run>> {
        return localDataSource.getAllRunSortedByCalories()
    }

    override fun getAllRunSortedByAvgSpeed(): LiveData<List<Run>> {
        return localDataSource.getAllRunSortedByAvgSpeed()
    }

    override fun getTotalAvgSpeed(): LiveData<Float> {
        return localDataSource.getTotalAvgSpeed()
    }

    override fun getTotalDistance(): LiveData<Int> {
        return localDataSource.getTotalDistance()
    }

    override fun getTotalCalories(): LiveData<Int> {
        return localDataSource.getTotalCalories()
    }

    override fun getTotalTimeInMillis(): LiveData<Long> {
        return localDataSource.getTotalTimeInMillis()
    }

}