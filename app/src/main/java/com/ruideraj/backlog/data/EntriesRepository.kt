package com.ruideraj.backlog.data

import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.Status
import com.ruideraj.backlog.injection.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface EntriesRepository {
    suspend fun loadEntriesForList(listId: Long): Flow<List<Entry>>
    suspend fun setStatusForEntry(entryId: Long, status: Status)
}

class EntriesRepositoryImpl @Inject constructor (private val entriesDao: EntriesDao,
                                                 @IoDispatcher private val ioDispatcher: CoroutineDispatcher)
    : EntriesRepository {

    override suspend fun loadEntriesForList(listId: Long) = entriesDao.getEntriesForList(listId)

    override suspend fun setStatusForEntry(entryId: Long, status: Status) {
        withContext(ioDispatcher) {
            entriesDao.updateEntryStatus(entryId, status)
        }
    }
}