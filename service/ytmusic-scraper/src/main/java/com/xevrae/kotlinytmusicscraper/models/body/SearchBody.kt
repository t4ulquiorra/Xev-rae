package com.xevrae.kotlinytmusicscraper.models.body

import com.xevrae.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String?,
    val params: String?,
)