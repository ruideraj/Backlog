package com.ruideraj.backlog.data

import androidx.room.ColumnInfo

data class PositionTuple(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "position") val position: Double
)

fun findNewPositionValue(positionList: List<PositionTuple>, itemId: Long, newPosition: Int): Double {
    val currentPosition = positionList.indexOfFirst { it.id == itemId }

    return when (newPosition) {
        0 -> positionList[0].position - 1
        positionList.size - 1 -> positionList[positionList.size - 1].position + 1
        else -> {
            val positionA = positionList[newPosition].position
            val positionB = if (newPosition > currentPosition) {
                positionList[newPosition + 1].position
            } else {
                positionList[newPosition - 1].position
            }

            (positionA + positionB) / 2
        }
    }
}