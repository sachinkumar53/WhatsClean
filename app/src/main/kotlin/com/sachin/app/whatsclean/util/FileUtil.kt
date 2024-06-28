package com.sachin.app.whatsclean.util

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.sachin.app.whatsclean.BuildConfig
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.MediaFile
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import com.sachin.app.whatsclean.util.extension.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object FileUtil {

    fun deleteByUriString(
        contentResolver: ContentResolver,
        uriString: String
    ): Boolean {
        return try {
            val uri = uriString.toUri()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                DocumentsContract.deleteDocument(contentResolver, uri)
            } else {
                uri.toFile().delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteByUriString: $uriString")
            false
        }
    }

    suspend fun startFileShareIntent(
        context: Context,
        fileList: List<MediaFile>
    ) = withContext(Dispatchers.IO) {

        val fileURIs = arrayListOf<Uri>()
        var mimeType: String? = null

        fileList.map {
            async {
                if (mimeType == null)
                    mimeType = it.mimeType

                if (isAndroidRorAbove) {
                    it.uriString.toUri()
                } else {
                    FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        it.uriString.toUri().toFile()
                    )
                }
            }
        }.awaitAll().toCollection(fileURIs)

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType ?: "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileURIs)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.share_file_using)
                )
            )
        } catch (e: ActivityNotFoundException) {
            withContext(Dispatchers.Main) {
                context.showToast(R.string.unable_to_share_file)
            }
        }
    }

    fun openFile(
        context: Context,
        file: MediaFile
    ) {
        try {
            Intent(Intent.ACTION_VIEW).apply {
                type = file.mimeType ?: "*/*"
                data = if (isAndroidRorAbove) file.uriString.toUri() else getFileProviderUri(
                    context,
                    file.uriString.toUri().toFile()
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

            }.also {
                context.startActivity(it)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "openFile: $file", e)
            context.showToast(R.string.unable_to_open_file)
        }
    }

    fun getApplicationInfoFromFile(
        context: Context,
        file: MediaFile
    ): ApplicationInfo? {
        val apkFile = if (isAndroidRorAbove) {
            val cacheDir = context.externalCacheDir
            val outputFile = File(cacheDir, file.name)
            if (!outputFile.exists()) {
                context.contentResolver.openFileDescriptor(
                    file.uriString.toUri(),
                    "r"
                )?.use {
                    val inputStream = FileInputStream(it.fileDescriptor)
                    inputStream.use { fis ->
                        fis.copyTo(outputFile.outputStream())
                    }
                }
            }
            outputFile
        } else file.uriString.toUri().toFile()

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


    private fun getFileProviderUri(context: Context, file: File): Uri? {
        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    suspend fun md5(
        contentResolver: ContentResolver,
        file: MediaFile
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (isAndroidRorAbove) {
                md5(contentResolver, file.uriString.toUri())
            } else {
                md5(file.uriString.toUri().toFile())
            }
        }.getOrNull()
    }

    private suspend fun md5(
        contentResolver: ContentResolver,
        uri: Uri
    ): String? = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("MD5")
        var md: String? = null
        runCatching {
            contentResolver.openInputStream(uri)?.use {
                val buffer = ByteArray(8192)
                generateSequence {
                    when (val bytesRead = it.read(buffer)) {
                        -1 -> null
                        else -> bytesRead
                    }
                }.forEach { bytesRead -> digest.update(buffer, 0, bytesRead) }
                md = digest.digest().joinToString("") { "%02x".format(it) }
            }
        }
        md
    }


    private suspend fun md5(
        file: File
    ): String? = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("MD5")
        try {
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                generateSequence {
                    when (val bytesRead = fis.read(buffer)) {
                        -1 -> null
                        else -> bytesRead
                    }
                }.forEach { bytesRead -> digest.update(buffer, 0, bytesRead) }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "md5: $file", e)
            null
        }
    }

    fun validateWhatsAppFolder(
        path: String
    ): Boolean {
        val sdCard = Environment.getExternalStorageDirectory()
        val wp = File(sdCard, "WhatsApp")
        val wpb = File(sdCard, "WhatsApp Business")
        return arrayOf(
            wp.absolutePath, wpb.absolutePath
        ).any { it.endsWith(path) }
    }

    fun deleteFileFromCache(
        context: Context,
        file: MediaFile
    ) {
        Log.i(TAG, "deleteFileFromCache: ${file.name}")
        if (!isAndroidRorAbove) return
        try {
            val cacheDir = context.externalCacheDir
            val cacheFile = File(cacheDir, file.name)
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        } catch (e: Exception) {
        }

    }

    private const val TAG = "FileUtil"
}