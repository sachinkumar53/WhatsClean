package com.sachin.app.glide_loaders.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.net.toFile
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream

object Util {

    private fun getNameFromUriString(
        uriString: String
    ) = uriString.substringAfterLast("%2F")

    fun getApplicationInfo(
        context: Context,
        fileUriString: String
    ): ApplicationInfo? {
        val apkFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cacheDir = context.externalCacheDir
            val name = getNameFromUriString(fileUriString)
            val outputFile = File(cacheDir, name)
            if (!outputFile.exists()) {
                context.contentResolver.openFileDescriptor(
                    fileUriString.toUri(),
                    "r"
                )?.use {
                    val inputStream = FileInputStream(it.fileDescriptor)
                    inputStream.use { fis ->
                        fis.copyTo(outputFile.outputStream())
                    }
                }
            }
            outputFile
        } else fileUriString.toUri().toFile()

        val path = apkFile.absolutePath

        val applicationInfo = context.packageManager.getPackageArchiveInfo(
            path,
            PackageManager.GET_META_DATA
        )?.applicationInfo?.apply {
            sourceDir = path
            publicSourceDir = path
        }

        return applicationInfo
    }


    fun deleteFileFromCache(
        context: Context,
        fileUriString: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        try {
            val name = getNameFromUriString(fileUriString)
            val cacheDir = context.externalCacheDir
            val cacheFile = File(cacheDir, name)
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        } catch (e: Exception) {
        }

    }

    private const val TAG = "Util"
}