package com.ruideraj.backlog.injection

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
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
    fun providesAppDatabase(@ApplicationContext context: Context, metadataConverters: MetadataConverters) =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .createFromAsset("database/testDb100.db")
            .addTypeConverter(metadataConverters)
            .build()

    @Provides
    fun providesListsDao(appDatabase: AppDatabase) = appDatabase.listsDao()

    @Provides
    fun providesEntriesDao(appDatabase: AppDatabase) = appDatabase.entriesDao()

    @Provides
    fun providesMetadataConverters(gson: Gson) = MetadataConverters(gson)

    @Provides
    fun providesGson() = Gson()

}