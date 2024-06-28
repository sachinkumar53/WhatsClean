package com.sachin.app.whatsclean.data.database

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.sachin.app.whatsclean.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MediaFilesDao {

    @Query("SELECT SUM(length) as totalSize, COUNT(*) as totalCount FROM media_files")
    abstract fun getTotalSizeAndCount(): Flow<SizeAndCount>

    @Query(
        "SELECT mediaType, SUM(length) as totalSize, COUNT(*) as totalCount " +
                "FROM media_files GROUP BY mediaType HAVING mediaType IS NOT NULL " +
                "ORDER BY mediaType"
    )
    abstract fun getTypesOfFileWithSizeAndCount(): Flow<List<SizeAndCountWithType>>


    @RawQuery(observedEntities = [SingleMediaFile::class])
    abstract fun getFilesViaQuery(query: SupportSQLiteQuery): Flow<List<SingleMediaFile>>

    @RawQuery
    abstract suspend fun getFilesViaQueryStatic(query: SupportSQLiteQuery): List<SingleMediaFile>

    fun getCategorizedFilesOfType(
        mediaType: MediaType,
        needSentFiles: Boolean,
        sortType: SortType
    ): Flow<List<SingleMediaFile>> {
        val needSent = if (needSentFiles) 1 else 0
        val query = "SELECT * FROM media_files WHERE mediaType = ? AND isSentFile = $needSent " +
                "ORDER BY ${sortType.toSqliteQuery()}"
        return getFilesViaQuery(
            query = SimpleSQLiteQuery(
                query,
                arrayOf<Any>(mediaType.name)
            )
        )
    }

    suspend fun getPreviewFilesOfTypeSortedBy(
        mediaType: MediaType,
        sortType: SortType
    ): List<SingleMediaFile> {
        val query = "SELECT * FROM media_files WHERE mediaType = ? " +
                "ORDER BY ${sortType.toSqliteQuery()} LIMIT 4"
        return getFilesViaQueryStatic(
            query = SimpleSQLiteQuery(
                query,
                arrayOf<Any>(mediaType.name)
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(files: List<SingleMediaFile>)

    @Query("DELETE FROM media_files")
    abstract suspend fun deleteAll()

    @Delete
    abstract suspend fun delete(file: SingleMediaFile)


    @Query("DELETE FROM media_files WHERE uriString = :uriString")
    abstract suspend fun deleteByUriString(uriString: String)

    @Query("SELECT * FROM media_files WHERE mediaType =:type")
    abstract suspend fun getAllFilesOfType(type: MediaType):List<SingleMediaFile>

    @Query("DELETE FROM media_files WHERE mediaType =:type")
    abstract fun deleteAllFilesOfType(type: MediaType)
}

fun SortType.toSqliteQuery(): String = when (this) {
    SortType.NEWEST_FIRST -> "lastModified DESC"
    SortType.OLDEST_FIRST -> "lastModified ASC"
    SortType.LARGEST_FIRST -> "length DESC"
    SortType.SMALLEST_FIRST -> "length ASC"
}
