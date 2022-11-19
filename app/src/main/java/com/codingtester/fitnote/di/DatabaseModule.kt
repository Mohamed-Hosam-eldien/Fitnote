package com.codingtester.fitnote.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.codingtester.fitnote.helper.Constants
import com.codingtester.fitnote.data.local.db.RunDatabase
import com.codingtester.fitnote.helper.Constants.SHARED_PREF_NAME
import com.codingtester.fitnote.helper.Constants.USER_FIRST_SIGNED
import com.codingtester.fitnote.helper.Constants.USER_NAME
import com.codingtester.fitnote.helper.Constants.USER_WEIGHT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        RunDatabase::class.java,
        Constants.DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideDao(db: RunDatabase) = db.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext app: Context): SharedPreferences =
        app.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideUserName(sharedPref: SharedPreferences) = sharedPref.getString(USER_NAME, "")

    @Provides
    @Singleton
    fun provideUserWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(USER_WEIGHT, 0f)

    @Provides
    @Singleton
    fun provideUserFirstSign(sharedPref: SharedPreferences) = sharedPref.getBoolean(USER_FIRST_SIGNED, true)

}