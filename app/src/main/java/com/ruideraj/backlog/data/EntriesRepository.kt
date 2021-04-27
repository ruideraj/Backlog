package com.ruideraj.backlog.data

import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.injection.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface EntriesRepository {
    suspend fun loadEntriesForList(listId: Long): Flow<List<Entry>>
}

class EntriesRepositoryImpl @Inject constructor (private val entriesDao: EntriesDao,
                                                 @IoDispatcher private val ioDispatcher: CoroutineDispatcher)
    : EntriesRepository {

    override suspend fun loadEntriesForList(listId: Long) = entriesDao.getEntriesForList(listId)

}