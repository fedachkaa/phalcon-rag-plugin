package com.fedachkaa.api

import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fedachkaa.config.PhalconRagConfig

class PhalconRagApiClient (
    private val baseUrl: String = PhalconRagConfig.API_BASE_URL
) {
    private val gson = Gson()
    private val httpClient = HttpClient.newHttpClient()

    fun ask(question: String, context: String? = null): AskResponse {
        val request = AskRequest(question)
        val json = gson.toJson(request)

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/v1/ask"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build()

        val response = httpClient.send(
            httpRequest,
            HttpResponse.BodyHandlers.ofString()
        )

        return gson.fromJson(
            response.body(),
            AskResponse::class.java
        )
    }
}