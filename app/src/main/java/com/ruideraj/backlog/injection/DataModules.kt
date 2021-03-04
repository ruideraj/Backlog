package com.ruideraj.backlog.injection

import com.ruideraj.backlog.lists.FakeListsRepository
import com.ruideraj.backlog.lists.ListsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ListsModule {

    @Binds
    @Singleton
    abstract fun bindListsRepository(fakeListsRepo: FakeListsRepository): ListsRepository

}