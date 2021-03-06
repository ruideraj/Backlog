package com.ruideraj.backlog.data

import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.Status
import com.ruideraj.backlog.data.local.EntriesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface EntriesRepository {
    suspend fun loadEntriesForList(listId: Long): Flow<List<Entry>>
    suspend fun createEntry(listId: Long, type: MediaType, title: String, metadata: Metadata)
    suspend fun setStatusForEntry(entryId: Long, status: Status)
    suspend fun editEntry(entryId: Long, title: String, metadata: Metadata)
    suspend fun moveEntry(entryId: Long, newPosition: Int)
    suspend fun deleteEntries(ids: List<Long>)
}

class EntriesRepositoryImpl @Inject constructor (private val entriesDao: EntriesDao)
    : EntriesRepository {

    override suspend fun loadEntriesForList(listId: Long) = entriesDao.getEntriesForList(listId)

    override suspend fun createEntry(
        listId: Long,
        type: MediaType,
        title: String,
        metadata: Metadata
    ) {
        val position = entriesDao.getMaxPositionForList(listId) + 1
        val newEntry = Entry(0, listId, title, type, position, metadata, Status.TODO)
        entriesDao.insertEntry(newEntry)
    }

    override suspend fun setStatusForEntry(entryId: Long, status: Status) {
        entriesDao.updateEntryStatus(entryId, status)
    }

    override suspend fun editEntry(entryId: Long, title: String, metadata: Metadata) {
        entriesDao.updateEntry(entryId, title, metadata)
    }

    override suspend fun moveEntry(entryId: Long, newPosition: Int) {
        val listId = entriesDao.getListIdForEntry(entryId)
        val entryPositions = entriesDao.getAllEntryPositionsForList(listId)

        val index = entryPositions.indexOfFirst { it.id == entryId }
        if (index == newPosition) {
            return
        }

        val newPositionValue = findNewPositionValue(entryPositions, entryId, newPosition)
        entriesDao.updateEntryPosition(entryId, newPositionValue)
    }

    override suspend fun deleteEntries(ids: List<Long>) {
        entriesDao.deleteEntries(ids)
    }
}