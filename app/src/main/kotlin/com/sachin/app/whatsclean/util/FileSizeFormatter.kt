package com.sachin.app.whatsclean.util

object FileSizeFormatter {

    private const val KB = 1024F
    private const val MB = KB * KB
    private const val GB = MB * KB

    fun format(size: Long, format: String = "%.2f"): FileSize {
        val bytes = size.toFloat()
        return when {
            (size < KB) -> FileSize(String.format(format, bytes), "B")
            (size >= KB && size < MB) -> FileSize(String.format(format, bytes.div(KB)), "KB")
            (size >= MB && size < GB) -> FileSize(String.format(format, bytes.div(MB)), "MB")
            (size >= GB) -> FileSize(String.format(format, bytes.div(GB)), "GB")
            else -> FileSize(String.format(format, bytes), "B")
        }
    }
}

data class FileSize(
    val size: String,
    val unit: String
) {

    override fun toString(): String {
        return "$size $unit"
    }
}