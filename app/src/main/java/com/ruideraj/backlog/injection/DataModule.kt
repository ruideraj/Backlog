package com.ruideraj.backlog.injection

import com.ruideraj.backlog.lists.FakeListsRepository
import com.ruideraj.backlog.lists.ListsRepository
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @Provides
    fun providesListsRepository(fakeListsRepo: FakeListsRepository): ListsRepository = fakeListsRepo

}