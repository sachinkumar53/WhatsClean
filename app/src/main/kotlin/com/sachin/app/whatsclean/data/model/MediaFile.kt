package com.sachin.app.whatsclean.data.model

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.provider.DocumentsContractCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.ui.media.getIconResIdFromExtension
import com.sachin.app.whatsclean.util.FileUtil
import com.sachin.app.whatsclean.util.extension.ioDispatcher
import com.sachin.app.whatsclean.util.extension.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.IOException

private const val TAG = "MediaFile"


sealed class MediaFile : Parcelable {
    abstract val name: String
    abstract val lastModified: Long
    abstract val uriString: String
    abstract val length: Long
    abstract val mimeType: String?
    abstract val mediaType: MediaType

    val extension: String
        get() = name.substringAfterLast('.', "")

    @CallSuper
    open suspend fun delete(
        contentResolver: ContentResolver
    ): Boolean = ioDispatcher {
        FileUtil.deleteByUriString(contentResolver, uriString)
    }
}

@Entity(tableName = "media_files")
@Parcelize
data class SingleMediaFile(
    override val name: String,
    override val lastModified: Long,
    @PrimaryKey(autoGenerate = false)
    override val uriString: String,
    override val length: Long,
    override val mimeType: String?,
    override val mediaType: MediaType,
    val isSentFile: Boolean = false
) : MediaFile() {

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun moveTo(
        contentResolver: ContentResolver,
        targetUri: Uri
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val sourceUri = uriString.toUri()
            val id = DocumentsContractCompat.getTreeDocumentId(targetUri)
                ?: throw IOException("Couldn't get tree document id.")
            val destinationUri = DocumentsContractCompat.buildDocumentUriUsingTree(
                targetUri,
                id
            ) ?: throw IOException("Couldn't build document uri using tree.")

            DocumentsContract.moveDocument(
                contentResolver,
                sourceUri,
                uriString.substringBeforeLast("%2F").toUri(),
                destinationUri
            ) != null
        }.onFailure {
            Log.e(TAG, "deleteFile: Error deleting $this", it)
        }.isSuccess
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun copyTo(
        contentResolver: ContentResolver,
        targetUri: Uri
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val sourceUri = uriString.toUri()
            val id = DocumentsContractCompat.getTreeDocumentId(targetUri)
                ?: throw IOException("Couldn't get tree document id.")
            val destinationUri = DocumentsContractCompat.buildDocumentUriUsingTree(
                targetUri,
                id
            ) ?: throw IOException("Couldn't build document uri using tree.")

            DocumentsContract.copyDocument(
                contentResolver,
                sourceUri,
                destinationUri
            ) != null
        }.onFailure {
            Log.e(TAG, "deleteFile: Error deleting $this", it)
        }.isSuccess
    }


    suspend fun moveTo(destinationFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = uriString.toUri().toFile()
            file.copyTo(destinationFile, overwrite = true)
            file.delete()
        } catch (e: IOException) {
            Log.e(TAG, "moveTo: Error moving $this", e)
            false
        }
    }

    companion object {

        fun fromFile(file: File, mediaType: MediaType): SingleMediaFile {
            return SingleMediaFile(
                name = file.name,
                lastModified = file.lastModified(),
                uriString = Uri.fromFile(file).toString(),
                length = file.length(),
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension),
                mediaType = mediaType,
                isSentFile = file.parent?.endsWith("sent", true) == true
            )
        }
    }
}

@Parcelize
data class DuplicateMediaFile(
    override val name: String,
    override val lastModified: Long,
    override val uriString: String,
    override val length: Long,
    override val mimeType: String?,
    override val mediaType: MediaType,
    val md5String: String?,
    @Ignore
    val copies: List<String>
) : MediaFile() {

    constructor(
        name: String,
        lastModified: Long,
        uriString: String,
        length: Long,
        mimeType: String?,
        mediaType: MediaType,
        md5String: String?,
    ) : this(name, lastModified, uriString, length, mimeType, mediaType, md5String, emptyList())

    val count: Int
        get() = copies.size + 1

    val totalLength: Long
        get() = length * count

    override suspend fun delete(
        contentResolver: ContentResolver
    ): Boolean = ioDispatcher {
        val result = mutableListOf(
            super.delete(contentResolver)
        )
        copies.mapTo(result) {
            FileUtil.deleteByUriString(contentResolver, it)
        }
        result.all { it }
    }

    suspend fun deleteCopies(
        contentResolver: ContentResolver
    ): Boolean = withContext(Dispatchers.IO) {
        copies.map {
            FileUtil.deleteByUriString(contentResolver, it)
        }.all { it }
    }
}

fun MediaFile.loadIcon(into: ImageView) {
    val isApkFile = extension == "apk"
    val isPdfFile = extension == "pdf"
    val isMediaFile = arrayOf(
        "video",
        "image"
    ).any {
        mimeType?.startsWith(it) == true
    }

    when (mediaType) {
        MediaType.AUDIO,
        MediaType.VOICE,
        MediaType.DATABASE -> {
            Glide.with(into)
                .load(mediaType.getDefaultIcon(into.context))
                .into(into)
        }

        else -> {
            when {
                isPdfFile -> {
                    Glide.with(into.context)
                        .asBitmap()
                        .load(uriString)
                        .signature(ObjectKey(lastModified.toString()))
                        .placeholder(R.drawable.pdf)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(into)
                }

                isApkFile -> {
                    Glide.with(into.context)
                        .asBitmap()
                        .load(uriString)
                        .signature(ObjectKey(lastModified.toString()))
                        .placeholder(R.drawable.apk)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(into)
                }

                isMediaFile -> {
                    into.load(
                        data = uriString,
                        placeholder = getIconResIdFromExtension(extension)
                    )
                }

                else -> {
                    Glide.with(into.context)
                        .load(getIconResIdFromExtension(extension))
                        .into(into)
                }
            }
        }
    }
}

object MediaDiffUtilCallback : DiffUtil.ItemCallback<MediaFile>() {
    override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
        return oldItem.uriString == newItem.uriString
    }

    override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
        return oldItem == newItem
    }

}
