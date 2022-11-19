package com.codingtester.fitnote.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingtester.fitnote.data.local.db.Run
import com.codingtester.fitnote.data.repositories.IMainRepository
import com.codingtester.fitnote.helper.SortedType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var mainRepo: IMainRepository
) : ViewModel() {

    fun insertRunDataToDB(run: Run) = viewModelScope.launch {
        mainRepo.insertRun(run)
    }

    private val getAllRunByDate = mainRepo.getAllRunSortedByDate()
    private val getAllRunByAvgSpeed = mainRepo.getAllRunSortedByAvgSpeed()
    private val getAllRunByDistance = mainRepo.getAllRunSortedByDistance()
    private val getAllRunByCalories = mainRepo.getAllRunSortedByCalories()
    private val getAllRunByTimeInMillis = mainRepo.getAllRunSortedByTimeInMillis()

    val runList = MediatorLiveData<List<Run>>()

    var sortType = SortedType.DATE

    init {
        runList.addSource(getAllRunByDate) { runs ->
            if(sortType == SortedType.DATE) {
                runs?.let { runList.value = it }
            }
        }
        runList.addSource(getAllRunByAvgSpeed) { runs ->
            if(sortType == SortedType.AVG_SPEED) {
                runs?.let { runList.value = it }
            }
        }
        runList.addSource(getAllRunByCalories) { runs ->
            if(sortType == SortedType.CALORIES) {
                runs?.let { runList.value = it }
            }
        }
        runList.addSource(getAllRunByTimeInMillis) { runs ->
            if(sortType == SortedType.TIME) {
                runs?.let { runList.value = it }
            }
        }
        runList.addSource(getAllRunByDistance) { runs ->
            if(sortType == SortedType.DISTANCE) {
                runs?.let { runList.value = it }
            }
        }
    }

    fun sortRun(type : SortedType) = when(type) {
        SortedType.DATE -> getAllRunByDate.value?.let { runList.value = it }
        SortedType.AVG_SPEED -> getAllRunByAvgSpeed.value?.let {
//            Log.e("TAG", "sortRun: " + it)
            runList.value = it
        }
        SortedType.CALORIES -> getAllRunByCalories.value?.let { runList.value = it }
        SortedType.TIME -> getAllRunByTimeInMillis.value?.let { runList.value = it }
        SortedType.DISTANCE -> getAllRunByDistance.value?.let { runList.value = it }
    }.also {
        sortType = type
    }

}