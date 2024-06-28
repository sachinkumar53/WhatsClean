package com.sachin.lib.apkdecoder

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import kotlinx.coroutines.*

class AppIconFetcher(
    private val packageManager: PackageManager,
    private val applicationInfo: ApplicationInfo
) : DataFetcher<Drawable> {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val appIconJobMap = mutableMapOf<String, Job?>()

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
        appIconJobMap[applicationInfo.packageName]?.cancel()
        appIconJobMap[applicationInfo.packageName] = coroutineScope.launch {
            try {
                val icon = packageManager.getApplicationIcon(applicationInfo)
                callback.onDataReady(icon)
            } catch (e: Exception) {
                callback.onLoadFailed(e)
            }
        }
    }

    override fun cleanup() {
        coroutineScope.cancel()
    }

    override fun cancel() {
        coroutineScope.cancel()
    }

    override fun getDataClass() = Drawable::class.java

    override fun getDataSource() = DataSource.LOCAL
}