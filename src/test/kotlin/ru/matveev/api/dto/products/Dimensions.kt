package ru.matveev.api.dto.products

import kotlinx.serialization.Serializable

@Serializable
data class Dimensions(
    val width: Double,
    val height: Double,
    val depth: Double,
)