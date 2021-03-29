package com.ruideraj.backlog.injection

import android.content.Context
import androidx.room.Room
import com.ruideraj.backlog.data.AppDatabase
import com.ruideraj.backlog.data.DATABASE_NAME
import com.ruideraj.backlog.lists.ListsRepository
import com.ruideraj.backlog.lists.ListsRepositoryImpl
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
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

}