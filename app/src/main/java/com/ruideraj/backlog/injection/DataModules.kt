package com.ruideraj.backlog.injection

import android.content.Context
import androidx.room.Room
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.data.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ListsModule {

    @Binds
    @Singleton
    abstract fun bindListsRepository(listsRepositoryImpl: ListsRepositoryImpl): ListsRepository

}

@Module
@InstallIn(SingletonComponent::class)
abstract class EntriesModule {

    @Binds
    @Singleton
    abstract fun bindEntriesRepository(entriesRepositoryImpl: EntriesRepositoryImpl): EntriesRepository

}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME).build()

    @Provides
    fun providesListsDao(appDatabase: AppDatabase) = appDatabase.listsDao()

    @Provides
    fun providesEntriesDao(appDatabase: AppDatabase) = appDatabase.entriesDao()

}