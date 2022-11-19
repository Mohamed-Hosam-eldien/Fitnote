package com.codingtester.fitnote.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.codingtester.fitnote.data.repositories.IMainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private var mainRepo: IMainRepository
) : ViewModel() {


}