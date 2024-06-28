package com.sachin.app.glide_loaders.apk

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.sachin.app.glide_loaders.util.Util
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ApkDataFetcher(
    private val context: Context,
    private val file: String,
    private val size: Size
) : DataFetcher<InputStream> {
    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null
    private var isFinished = false

    override fun loadData(
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>
    ) {
        currentJob = scope.launch {
            isFinished = false
            try {
                val appInfo = requireNotNull(Util.getApplicationInfo(context, file))
                val appIcon = context.packageManager.getApplicationIcon(appInfo)
                val bitmap = appIcon.toBitmap(
                    width = minOf(appIcon.intrinsicWidth, size.width),
                    height = minOf(appIcon.intrinsicHeight, size.height)
                )
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                val bis = ByteArrayInputStream(bos.toByteArray())
                isFinished = true
                callback.onDataReady(bis)
            } catch (e: Exception) {
                callback.onLoadFailed(e)
            }
        }
    }

    override fun cleanup() {
        if (isFinished) {
            Util.deleteFileFromCache(context, file)
        }
    }

    override fun cancel() {
        currentJob?.cancel()
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }

}
