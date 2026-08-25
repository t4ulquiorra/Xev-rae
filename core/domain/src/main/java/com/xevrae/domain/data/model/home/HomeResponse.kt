package com.xevrae.domain.data.model.home

import com.xevrae.domain.data.model.home.chart.Chart
import com.xevrae.domain.data.model.mood.Mood
import com.xevrae.domain.utils.Resource

data class HomeResponse(
    val homeItem: Resource<ArrayList<HomeItem>>,
    val exploreMood: Resource<Mood>,
    val exploreChart: Resource<Chart>,
)