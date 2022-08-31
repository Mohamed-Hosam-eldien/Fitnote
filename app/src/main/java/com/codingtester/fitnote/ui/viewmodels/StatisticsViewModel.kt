package com.codingtester.fitnote.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.codingtester.fitnote.data.repositories.IMainRepository
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private var mainRepo: IMainRepository
) : ViewModel() {


}