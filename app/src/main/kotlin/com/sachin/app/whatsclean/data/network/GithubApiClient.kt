package com.sachin.app.whatsclean.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


object GithubApiClient {
    private const val URL = "https://api.github.com/repos/sachinkumar53/whatsclean/releases/latest"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getLatestVersionFromGithub(): String {
        return client.get(URL).body<GithubReleaseDto>().tagName
    }


}

