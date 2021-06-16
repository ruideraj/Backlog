package com.ruideraj.backlog.data

import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.Status
import com.ruideraj.backlog.injection.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface EntriesRepository {
    suspend fun loadEntriesForList(listId: Long): Flow<List<Entry>>
    suspend fun createEntry(listId: Long, type: MediaType, title: String, metadata: Metadata)
    suspend fun setStatusForEntry(entryId: Long, status: Status)
    suspend fun editEntry(entryId: Long, title: String, metadata: Metadata)
    suspend fun deleteEntries(ids: List<Long>)
}

class EntriesRepositoryImpl @Inject constructor (private val entriesDao: EntriesDao,
                                                 @IoDispatcher private val ioDispatcher: CoroutineDispatcher)
    : EntriesRepository {

    override suspend fun loadEntriesForList(listId: Long) = entriesDao.getEntriesForList(listId)

    override suspend fun createEntry(
        listId: Long,
        type: MediaType,
        title: String,
        metadata: Metadata
    ) {
        withContext(ioDispatcher) {
            val position = entriesDao.getMaxPositionForList(listId) + 1
            val newEntry = Entry(0, listId, title, type, position, metadata, Status.TODO)
            entriesDao.insertEntry(newEntry)
        }
    }

    override suspend fun setStatusForEntry(entryId: Long, status: Status) {
        withContext(ioDispatcher) {
            entriesDao.updateEntryStatus(entryId, status)
        }
    }

    override suspend fun editEntry(entryId: Long, title: String, metadata: Metadata) {
        withContext(ioDispatcher) {
            entriesDao.editEntry(entryId, title, metadata)
        }
    }

    override suspend fun deleteEntries(ids: List<Long>) {
        withContext(ioDispatcher) {
            entriesDao.deleteEntries(ids)
        }
    }
}