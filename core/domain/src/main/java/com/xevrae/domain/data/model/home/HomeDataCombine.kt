package com.xevrae.domain.data.model.home

import com.xevrae.domain.data.model.home.chart.Chart
import com.xevrae.domain.utils.Resource

data class HomeDataCombine(
    val home: Resource<Pair<String?, List<HomeItem>>>,
    val chart: Resource<Chart>,
    val newRelease: Resource<List<HomeItem>>,
)