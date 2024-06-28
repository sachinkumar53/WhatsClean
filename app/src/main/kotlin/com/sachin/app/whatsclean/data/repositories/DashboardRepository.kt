package com.sachin.app.whatsclean.data.repositories

import android.content.Context
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.data.database.AppDatabase
import com.sachin.app.whatsclean.data.model.Card
import com.sachin.app.whatsclean.data.model.MediaType
import com.sachin.app.whatsclean.data.model.SizeAndCount
import com.sachin.app.whatsclean.data.model.titleResId
import com.sachin.app.whatsclean.data.source.MediaDatabaseBuilder
import com.sachin.app.whatsclean.util.extension.ioDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DashboardRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val databaseBuilder: MediaDatabaseBuilder,
    database: AppDatabase,
    settingsManager: SettingsManager
) {

    private val mediaFilesDao = database.mediaFilesDao

    private val sortTypeFlow = settingsManager.getSortType().distinctUntilChanged()

    fun getLoadStatus(): Flow<LoadStatus> = databaseBuilder.getLoadStatus()

    fun reloadAllFiles() = databaseBuilder.reloadAllFiles()

    fun getTotalSizeAndCount(): Flow<SizeAndCount> =
        mediaFilesDao.getTotalSizeAndCount().distinctUntilChanged()

    fun getCards(): Flow<List<Card>> = sortTypeFlow.flatMapLatest { sortType ->
        mediaFilesDao.getTypesOfFileWithSizeAndCount().mapLatest {
            ioDispatcher {
                it.map {
                    async {
                        val previews = mediaFilesDao.getPreviewFilesOfTypeSortedBy(
                            it.mediaType,
                            sortType
                        )

                        Card(
                            titleResId = it.mediaType.titleResId,
                            type = it.mediaType,
                            size = it.sizeAndCount.totalSize,
                            count = it.sizeAndCount.totalCount,
                            previewList = previews
                        )
                    }
                }.awaitAll()
            }
        }.flowOn(IO)
    }.flowOn(IO)

    suspend fun deleteAllFilesOfType(types: List<MediaType>) = ioDispatcher {
        types.map { type ->
            mediaFilesDao.getAllFilesOfType(type).forEach {
                it.delete(contentResolver = context.contentResolver)
            }
            mediaFilesDao.deleteAllFilesOfType(type)
        }
    }
}


enum class LoadStatus {
    NOT_LOADING, LOADING, LOADED
}