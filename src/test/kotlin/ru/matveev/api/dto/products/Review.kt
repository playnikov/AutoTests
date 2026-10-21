package ru.matveev.api.dto.products

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String,
    val reviewerEmail: String
)
