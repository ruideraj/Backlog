package com.ruideraj.backlog.injection

import com.ruideraj.backlog.util.Strings
import com.ruideraj.backlog.util.StringsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StringsModule {
    @Binds
    @Singleton
    abstract fun bindStrings(stringsImpl: StringsImpl): Strings
}