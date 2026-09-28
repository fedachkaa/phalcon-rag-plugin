package com.fedachkaa.api

data class AskRequest(
    val question: String
)

data class SourceResponse(
    val id: String,
    val type: String,
    val file: String,
    val section: String?,
    val method: String?
)

data class AskResponse(
    val answer: String,
    val sources: List<SourceResponse>
)