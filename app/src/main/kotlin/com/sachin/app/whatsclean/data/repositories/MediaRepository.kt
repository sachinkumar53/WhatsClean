package com.sachin.app.whatsclean.data.repositories

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.data.database.AppDatabase
import com.sachin.app.whatsclean.data.model.*
import com.sachin.app.whatsclean.data.module.ApplicationScope
import com.sachin.app.whatsclean.util.extension.ioDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @ApplicationScope
    private val coroutineScope: CoroutineScope,
    database: AppDatabase,
    settingsManager: SettingsManager
) {

    private val mediaFilesDao = database.mediaFilesDao
    private val duplicateFilesDao = database.duplicateFilesDao
    private val sortTypeFlow = settingsManager.getSortType().distinctUntilChanged()

    fun getMediaFilesOfType(
        gridType: GridType,
        mediaType: MediaType
    ): Flow<List<MediaFile>> = when (gridType) {
        GridType.RECEIVED -> getReceivedFilesOfType(mediaType)
        GridType.SENT -> getSentFilesOfType(mediaType)
        GridType.DUPLICATES -> getDuplicateFilesOfType(mediaType)
    }.flowOn(IO)

    fun getImageFilesFlowByType(
        gridType: GridType,
        mediaType: MediaType
    ) = getMediaFilesOfType(
        gridType,
        mediaType
    ).mapLatest { list ->
        list.filter { it.mimeType?.startsWith("image") == true }
    }.flowOn(IO)

    private fun getReceivedFilesOfType(
        mediaType: MediaType
    ) = sortTypeFlow.flatMapLatest { sortType ->
        mediaFilesDao.getCategorizedFilesOfType(mediaType, false, sortType)
    }.flowOn(IO)

    private fun getSentFilesOfType(
        mediaType: MediaType
    ) = sortTypeFlow.flatMapLatest { sortType ->
        mediaFilesDao.getCategorizedFilesOfType(mediaType, true, sortType)
    }.flowOn(IO)

    private fun getDuplicateFilesOfType(
        mediaType: MediaType
    ): Flow<List<DuplicateMediaFile>> = sortTypeFlow.flatMapLatest { sortType ->
        duplicateFilesDao.getDuplicateMd5StringOfType(mediaType).mapLatest { md5List ->
            ioDispatcher {
                md5List.map { md5 ->
                    val duplicates = duplicateFilesDao.getDuplicateByMd5String(md5, sortType)
                    val original = duplicates.firstOrNull() ?: return@map null
                    val copies = duplicates.drop(1).map { it.uriString }

                    DuplicateMediaFile(
                        original.name,
                        original.lastModified,
                        original.uriString,
                        original.length,
                        original.mimeType,
                        mediaType,
                        md5,
                        copies
                    )
                }.filterNotNull()
            }
        }.flowOn(IO)
    }.flowOn(IO)


    fun deleteCopies(
        files: List<DuplicateMediaFile>,
        onComplete: ((isSuccess: Boolean) -> Unit)? = null
    ) = coroutineScope.launch(IO) {
        val result = files.map { file ->
            val result = file.deleteCopies(contentResolver = context.contentResolver)
            file.copies.forEach {
                mediaFilesDao.deleteByUriString(it)
                duplicateFilesDao.deleteByUriString(it)
            }
            result
        }.all { it }

        withContext(Dispatchers.Main) {
            onComplete?.invoke(result)
        }
    }


    fun deleteFiles(
        files: List<MediaFile>,
        onComplete: ((isSuccess: Boolean) -> Unit)? = null
    ) = coroutineScope.launch(IO) {
        val finalResult = files.map { file ->
            val result = file.delete(contentResolver = context.contentResolver)
            if (file is SingleMediaFile) {
                mediaFilesDao.delete(file)
                duplicateFilesDao.deleteByUriString(file.uriString)
            } else if (file is DuplicateMediaFile) {
                mediaFilesDao.deleteByUriString(file.uriString)
                duplicateFilesDao.deleteByUriString(file.uriString)
                file.copies.forEach {
                    mediaFilesDao.deleteByUriString(it)
                    duplicateFilesDao.deleteByUriString(it)
                }
            }
            result
        }.all { it }

        withContext(Dispatchers.Main) {
            onComplete?.invoke(finalResult)
        }
    }

    fun moveFiles(
        files: List<SingleMediaFile>,
        destinationFile: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ),
        onComplete: ((isSuccess: Boolean) -> Unit)? = null
    ) {
        coroutineScope.launch(IO) {
            val folder = File(destinationFile, "WhatsClean")

            if (!folder.exists())
                if (!folder.mkdirs()) {
                    withContext(Dispatchers.Main) { onComplete?.invoke(false) }
                    return@launch
                }

            val result = files.map { file ->
                val moved = file.moveTo(destinationFile)
                if (moved) mediaFilesDao.delete(file)
                moved
            }.all { it }
            withContext(Dispatchers.Main) { onComplete?.invoke(result) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun moveFiles(
        files: List<SingleMediaFile>,
        destinationTreeUri: Uri,
        onComplete: ((isSuccess: Boolean) -> Unit)? = null
    ) {
        coroutineScope.launch(IO) {
            val result = files.map { file ->
                val moved = file.moveTo(
                    contentResolver = context.contentResolver,
                    destinationTreeUri
                )
                if (moved) mediaFilesDao.delete(file)
                moved
            }.all { it }

            withContext(Dispatchers.Main) { onComplete?.invoke(result) }
        }
    }

}