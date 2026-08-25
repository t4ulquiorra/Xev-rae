package com.xevrae.data.di

import com.xevrae.data.db.DatabaseDao
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.xevrae.common.DB_NAME
import com.xevrae.data.dataStore.DataStoreManagerImpl
import com.xevrae.data.dataStore.createDataStoreInstance
import com.xevrae.data.db.Converters
import com.xevrae.data.db.MusicDatabase
import com.xevrae.data.db.datasource.AnalyticsDatasource
import com.xevrae.data.db.datasource.LocalDataSource
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.kotlinytmusicscraper.YouTube
import com.xevrae.spotify.Spotify
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import org.xevrae.aiservice.AiClient
import org.xevrae.lyrics.XevraeLyricsClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideConverters(): Converters = Converters()

    @Provides
    @Singleton
    fun provideMusicDatabase(
        @ApplicationContext context: Context,
        converters: Converters,
    ): MusicDatabase {
        return Room.databaseBuilder(context, MusicDatabase::class.java, DB_NAME)
            .addTypeConverter(converters)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabaseDao(database: MusicDatabase): DatabaseDao = database.getDatabaseDao()

    @Provides
    @Singleton
    fun provideLocalDataSource(databaseDao: DatabaseDao): LocalDataSource = LocalDataSource(databaseDao)

    @Provides
    @Singleton
    fun provideAnalyticsDatasource(databaseDao: DatabaseDao): AnalyticsDatasource = AnalyticsDatasource(databaseDao)

    @Provides
    @Singleton
    fun provideDataStore(): DataStore<Preferences> = createDataStoreInstance()

    @Provides
    @Singleton
    fun provideDataStoreManager(dataStore: DataStore<Preferences>): DataStoreManager = DataStoreManagerImpl(dataStore)

    @Provides
    @Singleton
    fun provideYouTube(): YouTube = YouTube()

    @Provides
    @Singleton
    fun provideSpotify(): Spotify = Spotify()

    @Provides
    @Singleton
    fun provideAiClient(): AiClient = AiClient()

    @Provides
    @Singleton
    fun provideXevraeLyricsClient(): XevraeLyricsClient = XevraeLyricsClient()
}
