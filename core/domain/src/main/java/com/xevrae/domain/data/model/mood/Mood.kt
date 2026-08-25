package com.xevrae.domain.data.model.mood

import kotlinx.serialization.Serializable

@Serializable
data class Mood(
    val sections: List<MoodSection>,
)

@Serializable
data class MoodSection(
    val title: String,
    val items: List<MoodItem>,
)

@Serializable
data class MoodItem(
    val title: String,
    val params: String,
    val stripeColor: Long? = null,
)