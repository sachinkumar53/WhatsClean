package com.sachin.app.whatsclean.data.source

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import androidx.annotation.RequiresApi
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.data.database.AppDatabase
import com.sachin.app.whatsclean.data.model.DuplicateEntity
import com.sachin.app.whatsclean.data.model.MediaType
import com.sachin.app.whatsclean.data.model.SingleMediaFile
import com.sachin.app.whatsclean.data.module.ApplicationScope
import com.sachin.app.whatsclean.data.repositories.LoadStatus
import com.sachin.app.whatsclean.util.FileUtil
import com.sachin.app.whatsclean.util.extension.ioDispatcher
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis


@Singleton
class MediaDatabaseBuilder @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @ApplicationScope
    private val coroutineScope: CoroutineScope,
    database: AppDatabase,
    settingsManager: SettingsManager
) {
    private val mediaFilesDao = database.mediaFilesDao
    private val duplicateFilesDao = database.duplicateFilesDao
    private val treeUriFlow = settingsManager.getTreeUri().distinctUntilChanged()
    private val whatsAppFolderFlow = settingsManager.getWhatsAppDirectory().distinctUntilChanged()

    private var duplicateSearchJob: Job? = null
    private var filesLoadJob: Job? = null

    private val _loadStatus: MutableStateFlow<LoadStatus> = MutableStateFlow(LoadStatus.NOT_LOADING)
    fun getLoadStatus(): Flow<LoadStatus> = _loadStatus.asStateFlow()

    init {
        coroutineScope.launch {
            if (isAndroidRorAbove) {
                treeUriFlow.collectLatest {
                    if (it != null) reloadAllFiles()
                    Log.i(TAG, "Uri changed to: $it")
                }
            } else {
                whatsAppFolderFlow.collectLatest {
                    reloadAllFiles()
                    Log.i(TAG, "Path changed to: $it")
                }
            }
        }

    }

    fun reloadAllFiles() {
        _loadStatus.value = LoadStatus.LOADING
        filesLoadJob?.cancel()
        filesLoadJob = rebuildDatabase()
    }

    private fun rebuildDatabase() = coroutineScope.launch {
        var files: List<List<SingleMediaFile>>
        val time = measureTimeMillis {
            mediaFilesDao.deleteAll()
            files = loadAllFiles()
            _loadStatus.value = LoadStatus.LOADED
        }

        duplicateSearchJob?.cancel()
        duplicateSearchJob = launch {
            generateAndSaveDuplicates(files)
        }

        Log.i(TAG, "loadAllFiles: Completed in $time ms.")
    }

    private suspend fun loadAllFiles(): List<List<SingleMediaFile>> = ioDispatcher {
        if (isAndroidRorAbove) {
            val treeUri = treeUriFlow.first()
            if (treeUri == null) {
                Log.e(TAG, "loadAllDocumentFiles: Tree uri is null")
                emptyList()
            } else {

                MediaType.values().map { mediaType ->
                    async {
                        loadMediaFromTreeUri(treeUri, mediaType).also {
                            mediaFilesDao.insertAll(it)
                        }
                    }
                }.awaitAll()

            }
        } else {

            val whatsAppPath = whatsAppFolderFlow.first()
            val whatsAppFolder = File(whatsAppPath)

            MediaType.values().map {
                async {
                    loadMediaFromFile(whatsAppFolder, it).also {
                        mediaFilesDao.insertAll(it)
                    }
                }
            }.awaitAll()
        }

    }

    private suspend fun generateAndSaveDuplicates(
        files: List<List<SingleMediaFile>>
    ) = ioDispatcher {
        val time = measureTimeMillis {
            files.asSequence().flatten().groupBy {
                it.length
            }.filterValues {
                it.size > 1
            }.map {
                it.value
            }.map {
                async {
                    it.map {
                        async {
                            val md5 = FileUtil.md5(context.contentResolver, it)
                            DuplicateEntity(it.uriString, it.mediaType, md5)
                        }
                    }.awaitAll().also {
                        duplicateFilesDao.insertAll(it)
                    }
                }
            }.toList().awaitAll()
        }
        Log.i(TAG, "generateAndSaveDuplicates: Saving all files took $time ms.")
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun loadMediaFromTreeUri(
        treeUri: Uri,
        mediaType: MediaType
    ): List<SingleMediaFile> = ioDispatcher {
        val deferred = mutableListOf(
            async {
                getFilesFromTreeUri(treeUri, mediaType.path, mediaType)
            }
        )

        mediaType.privateDir?.let { privateDir ->
            deferred += async {
                getFilesFromTreeUri(treeUri, privateDir, mediaType)
            }
        }

        mediaType.sentDir?.let { sentDir ->
            deferred += async {
                getFilesFromTreeUri(treeUri, sentDir, mediaType)
            }
        }

        deferred.awaitAll().flatten()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun getFilesFromTreeUri(
        treeUri: Uri,
        path: String,
        mediaType: MediaType
    ): List<SingleMediaFile> = ioDispatcher {
        try {
            val files = mutableListOf<SingleMediaFile>()
            context.contentResolver.query(
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    "primary:Android/media/com.whatsapp/WhatsApp/$path"
                ),
                arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null,
                null,
                null
            )?.use {
                while (it.moveToNext()) {
                    val name = it.getString(0)
                    val id = it.getString(1)
                    val lastModified = it.getLong(2)
                    val size = it.getLong(3)
                    val mimeType = it.getString(4)

                    if (mimeType != DocumentsContract.Document.MIME_TYPE_DIR && !name.startsWith(".nomedia")) {
                        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)
                        files += SingleMediaFile(
                            name = name,
                            lastModified = lastModified,
                            uriString = uri.toString(),
                            length = size,
                            mediaType = mediaType,
                            mimeType = mimeType,
                            isSentFile = path.endsWith("Sent", true)
                        )
                    }
                }
            }
            files
        } catch (e: Exception) {
            Log.e(TAG, "getFilesFromTreeUri: Couldn't get file from path: $path", e)
            emptyList()
        }
    }

    private suspend fun loadMediaFromFile(
        parent: File,
        mediaType: MediaType
    ): List<SingleMediaFile> = ioDispatcher {
        val deferred = mutableListOf(
            async {
                getFolderInfoFromFile(File(parent, mediaType.path), mediaType)
            }
        )

        mediaType.sentDir?.let { sentDir ->
            getFolderInfoFromFile(File(parent, sentDir), mediaType)
        }

        deferred.awaitAll().flatten()
    }

    private suspend fun getFolderInfoFromFile(
        folder: File,
        mediaType: MediaType
    ): List<SingleMediaFile> = ioDispatcher {
        val list = folder.listFiles() ?: return@ioDispatcher emptyList()
        val files = mutableListOf<SingleMediaFile>()
        for (file in list) {
            if (file.name.startsWith(".nomedia") || file.name.endsWith("Sent"))
                continue
            else if (file.isFile && !file.isHidden)
                files += SingleMediaFile.fromFile(file, mediaType)
            else if (file.isDirectory) {
                files += getFolderInfoFromFile(file, mediaType)
            }
        }
        files
    }
}

private const val TAG = "MediaDatabaseBuilder"