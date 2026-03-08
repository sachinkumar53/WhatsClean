package com.sachin.app.whatsclean.data.network


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class GithubReleaseDto(
    @SerialName("tag_name")
    val tagName: String
)