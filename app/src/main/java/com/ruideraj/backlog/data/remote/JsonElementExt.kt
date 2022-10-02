package com.ruideraj.backlog.data.remote

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

fun <R> JsonObject.parseIfNotNull(memberName: String, action: (JsonElement) -> R): R? {
    val element = get(memberName)
    return if (element != null && element !is JsonNull) {
        action(element)
    } else {
        null
    }
}