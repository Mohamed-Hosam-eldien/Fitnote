package com.codingtester.fitnote.di

import com.codingtester.fitnote.data.repositories.IMainRepository
import com.codingtester.fitnote.data.repositories.MainRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {

    @Binds
    fun provideProductRepository(
        repository: MainRepository
    ): IMainRepository

}