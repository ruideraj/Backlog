package com.ruideraj.backlog.injection

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ruideraj.backlog.BuildConfig
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.Constants.API_IGDB
import com.ruideraj.backlog.Constants.API_MOVIES
import com.ruideraj.backlog.Constants.API_MOVIES_SHORT
import com.ruideraj.backlog.Constants.API_OPEN_LIBRARY
import com.ruideraj.backlog.Constants.PROP_RAPIDAPI_KEY
import com.ruideraj.backlog.Constants.PROP_TWITCH_ID
import com.ruideraj.backlog.Constants.PROP_TWITCH_TOKEN
import com.ruideraj.backlog.data.*
import com.ruideraj.backlog.data.local.AppDatabase
import com.ruideraj.backlog.data.local.MetadataConverters
import com.ruideraj.backlog.data.local.YearConverter
import com.ruideraj.backlog.data.remote.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Year
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ListsModule {

    @Binds
    @Singleton
    abstract fun bindListsRepository(listsRepositoryImpl: ListsRepositoryImpl): ListsRepository

}

@Module
@InstallIn(SingletonComponent::class)
abstract class EntriesModule {

    @Binds
    @Singleton
    abstract fun bindEntriesRepository(entriesRepositoryImpl: EntriesRepositoryImpl): EntriesRepository

}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context, metadataConverters: MetadataConverters) =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .createFromAsset("database/testDb5.db")
            .addTypeConverter(metadataConverters)
            .build()

    @Provides
    fun providesListsDao(appDatabase: AppDatabase) = appDatabase.listsDao()

    @Provides
    fun providesEntriesDao(appDatabase: AppDatabase) = appDatabase.entriesDao()

    @Provides
    fun providesMetadataConverters(gson: Gson) = MetadataConverters(gson)

    @Provides
    fun providesGson(): Gson {
        val gsonBuilder = GsonBuilder().apply {
            registerTypeAdapter(Year::class.java, YearConverter())
        }
        return gsonBuilder.create()
    }

}

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

    @Provides
    @Singleton
    fun providesSearchRepository(searchRepositoryImpl: SearchRepositoryImpl): SearchRepository = searchRepositoryImpl

    @Provides
    @Singleton
    fun providesMoviesApi(propertiesReader: PropertiesReader): MoviesApi {
        val httpClientBuilder = OkHttpClient.Builder()

        addDebugLogging(httpClientBuilder)

        httpClientBuilder.addInterceptor { chain ->
            val originalRequest = chain.request()

            val requestBuilder = originalRequest.newBuilder()
                .header("x-rapidapi-host", API_MOVIES_SHORT)
                .header("x-rapidapi-key", propertiesReader.getProperty(PROP_RAPIDAPI_KEY))

            chain.proceed(requestBuilder.build())
        }

        val gson = GsonBuilder().apply {
            registerTypeAdapter(MoviesSearchResponse::class.java, MoviesSearchDeserializer())
            registerTypeAdapter(MoviesDetailsResponse::class.java, MoviesDetailsDeserializer())
        }.create()

        return Retrofit.Builder()
            .baseUrl(API_MOVIES)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClientBuilder.build())
            .build().create(MoviesApi::class.java)
    }

    @Provides
    @Singleton
    fun providesIgdbApi(propertiesReader: PropertiesReader): IgdbApi {
        val httpClientBuilder = OkHttpClient.Builder()

        addDebugLogging(httpClientBuilder)

        httpClientBuilder.addInterceptor { chain ->
            val originalRequest = chain.request()

            val requestBuilder = originalRequest.newBuilder()
                .header("Client-ID", propertiesReader.getProperty(PROP_TWITCH_ID))
                .header("Authorization", "Bearer " + propertiesReader.getProperty(PROP_TWITCH_TOKEN))
                .header("Accept", "application/json")

            chain.proceed(requestBuilder.build())
        }

        val gson = GsonBuilder().apply {
            registerTypeAdapter(IgdbResponse::class.java, IgdbDeserializer())
        }.create()

        return Retrofit.Builder()
            .baseUrl(API_IGDB)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClientBuilder.build())
            .build().create(IgdbApi::class.java)
    }

    @Provides
    @Singleton
    fun providesOpenLibraryApi(): OpenLibraryApi {
        val httpClientBuilder = OkHttpClient.Builder()

        addDebugLogging(httpClientBuilder)

        val gson = GsonBuilder().apply {
            registerTypeAdapter(OpenLibraryResponse::class.java, OpenLibraryDeserializer())
        }.create()

        return Retrofit.Builder()
            .baseUrl(API_OPEN_LIBRARY)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClientBuilder.build())
            .build().create(OpenLibraryApi::class.java)
    }

    private fun addDebugLogging(httpClientBuilder: OkHttpClient.Builder) {
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PropertiesModule {

    @Binds
    @Singleton
    abstract fun bindsPropertiesReader(propertiesReaderImpl: PropertiesReaderImpl): PropertiesReader

}