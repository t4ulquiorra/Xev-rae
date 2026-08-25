package com.xevrae.domain.data.model.mood

import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    val params: String,
    val title: String,
)