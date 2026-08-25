package com.xevrae.android.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.xevrae.android.domain.manager.DataStoreManager
import com.xevrae.android.data.datastore.DataStoreManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindDataStoreManager(
        dataStoreManagerImpl: DataStoreManagerImpl
    ): DataStoreManager
}
