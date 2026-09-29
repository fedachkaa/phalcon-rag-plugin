package com.fedachkaa.api

import com.google.gson.annotations.SerializedName

data class AskRequest(
    val question: String
)

data class SourceResponse(
    val id: String,
    val type: String,
    val file: String,
    val section: String? = null,
    val method: String? = null,
    @SerializedName("start_line")
    val startLine: Int? = null,
    @SerializedName("end_line")
    val endLine: Int? = null
)

data class AskResponse(
    val answer: String,
    val sources: List<SourceResponse>
)