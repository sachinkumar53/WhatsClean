package com.sachin.app.whatsclean.data.model

import androidx.annotation.StringRes
import com.sachin.app.whatsclean.util.FileSizeFormatter

data class Card(
    @StringRes
    val titleResId: Int,
    val type: MediaType,
    val size: Long,
    val count: Int,
    val previewList: List<SingleMediaFile> = emptyList()
) {
    val formattedSize: String
        get() = FileSizeFormatter.format(size).toString()
}