package com.ruideraj.backlog

object Constants {
    const val DATABASE_NAME = "backlogDatabase"
    const val TABLE_NAME_LISTS = "lists"
    const val TABLE_NAME_ENTRIES = "entries"

    const val PROPS_FILE_NAME = "config.properties"
    const val PROP_RAPIDAPI_KEY = "rapidApi"
    const val PROP_TWITCH_ID = "twitchClientId"
    const val PROP_TWITCH_TOKEN = "twitchAccessToken"

    const val API_MOVIES = "https://movie-database-alternative.p.rapidapi.com/"
    const val API_MOVIES_SHORT = "movie-database-alternative.p.rapidapi.com"
    const val API_IGDB = "https://api.igdb.com/v4/"
    const val API_OPEN_LIBRARY = "https://openlibrary.org/"

    const val ARG_LIST = "list"
    const val ARG_ENTRY = "entry"
    const val ARG_MODE = "mode"
    const val ARG_TITLE = "title"
    const val ARG_ICON = "icon"
    const val ARG_LIST_ID = "listId"
    const val ARG_TYPE = "type"
    const val ARG_SHOW_APP_BAR = "showAppBar"
    const val ARG_COUNT = "count"
    const val ARG_SEARCH_RESULT = "searchResult"

    const val MODE_CREATE = 0
    const val MODE_EDIT = 1
}