package com.codingtester.fitnote.di

import com.codingtester.fitnote.data.local.ILocalDataSource
import com.codingtester.fitnote.data.local.LocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface DataSourceModule {

    @Binds
    fun provideLocalDataSource(
        localDataSource: LocalDataSource
    ): ILocalDataSource

}