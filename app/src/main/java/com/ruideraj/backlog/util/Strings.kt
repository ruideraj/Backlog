package com.ruideraj.backlog.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface Strings {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg args: Any): String
}

class StringsImpl @Inject constructor(@ApplicationContext private val context: Context) : Strings {
    override fun getString(resId: Int) = context.getString(resId)
    override fun getString(resId: Int, vararg args: Any) = context.getString(resId, *args)
}