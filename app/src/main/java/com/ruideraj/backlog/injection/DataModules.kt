package com.ruideraj.backlog.injection

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ruideraj.backlog.BuildConfig
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.Constants.API_OPEN_LIBRARY
import com.ruideraj.backlog.Constants.API_RAWG
import com.ruideraj.backlog.data.*
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
            .createFromAsset("database/testDb100.db")
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
    fun providesRawgApi(): RawgApi {
        val httpClientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }

        val gson = GsonBuilder().apply {
            registerTypeAdapter(RawgResponse::class.java, RawgDeserializer())
        }.create()

        return Retrofit.Builder()
            .baseUrl(API_RAWG)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClientBuilder.build())
            .build().create(RawgApi::class.java)
    }

    @Provides
    @Singleton
    fun providesOpenLibraryApi(): OpenLibraryApi {
        val httpClientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }

        val gson = GsonBuilder().apply {
            registerTypeAdapter(OpenLibraryResponse::class.java, OpenLibraryDeserializer())
        }.create()

        return Retrofit.Builder()
            .baseUrl(API_OPEN_LIBRARY)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClientBuilder.build())
            .build().create(OpenLibraryApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PropertiesModule {

    @Binds
    @Singleton
    abstract fun bindsPropertiesReader(propertiesReaderImpl: PropertiesReaderImpl): PropertiesReader

}