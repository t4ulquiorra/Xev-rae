package com.xevrae.android.media.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    // TODO: Add providers for CoroutineScope (@Named(Config.SERVICE_SCOPE)), DatabaseProvider, SimpleCache, AudioAttributes, DefaultRenderersFactory
}
