package com.ruideraj.backlog.data

import android.content.Context
import com.ruideraj.backlog.Constants.PROPS_FILE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

interface PropertiesReader {
    fun getProperty(key: String): String
}

class PropertiesReaderImpl @Inject constructor(@ApplicationContext private val context: Context) : PropertiesReader {

    override fun getProperty(key: String): String {
        val properties = Properties()
        val assetManager = context.assets

        val inputStream = assetManager.open(PROPS_FILE_NAME)
        properties.load(inputStream)

        return properties.getProperty(key)
    }
}