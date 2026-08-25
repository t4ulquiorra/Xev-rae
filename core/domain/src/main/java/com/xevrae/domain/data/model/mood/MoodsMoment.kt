package com.xevrae.domain.data.model.mood

import kotlinx.serialization.Serializable

@Serializable
data class MoodsMoment(
    val params: String,
    val title: String,
)