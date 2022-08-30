package com.codingtester.fitnote.di

import android.content.Context
import androidx.room.Room
import com.codingtester.fitnote.common.Constants
import com.codingtester.fitnote.data.local.db.RunDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        RunDatabase::class.java,
        Constants.DATABASE_NAME
    )

    @Singleton
    @Provides
    fun provideDao(db: RunDatabase) = db.getRunDao()

}