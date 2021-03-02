package com.ruideraj.backlog.injection

import com.ruideraj.backlog.lists.ListsViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class])
interface AppComponent {

    fun listsViewModel(): ListsViewModel

}