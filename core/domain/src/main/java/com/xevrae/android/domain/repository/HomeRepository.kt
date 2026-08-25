package com.xevrae.android.domain.repository

import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun getHomeData(): Flow<Result<Any>>
    suspend fun getCharts(): Flow<Result<Any>>
}
