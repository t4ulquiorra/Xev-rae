package com.xevrae.kotlinytmusicscraper.models.body

import com.xevrae.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)