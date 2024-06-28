package com.sachin.app.whatsclean.data.database

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sachin.app.whatsclean.data.model.DuplicateEntity
import com.sachin.app.whatsclean.data.model.DuplicateMediaFile
import com.sachin.app.whatsclean.data.model.MediaType
import com.sachin.app.whatsclean.data.model.SortType
import kotlinx.coroutines.flow.Flow

@Dao
interface DuplicateFilesDao {

    @Query(
        "SELECT md5String FROM duplicates WHERE mediaType =:mediaType " +
                "GROUP BY md5String HAVING COUNT(*) > 1 "
    )
    fun getDuplicateMd5StringOfType(mediaType: MediaType): Flow<List<String>>

    /*@Query(
        "SELECT * FROM duplicates d " +
                "JOIN media_files m ON m.uriString = d.uriString " +
                "WHERE md5String =:md5"
    )*/

    @RawQuery
    suspend fun getDuplicateByMd5StringViaQuery(query:SimpleSQLiteQuery): List<DuplicateMediaFile>

    suspend fun getDuplicateByMd5String(
        md5: String?,
        sortType: SortType
    ): List<DuplicateMediaFile> {
        val query = "SELECT * FROM duplicates d " +
                "JOIN media_files m ON m.uriString = d.uriString " +
                "WHERE md5String =:md5 " +
                "ORDER BY ${sortType.toSqliteQuery()}"

        return getDuplicateByMd5StringViaQuery(SimpleSQLiteQuery(query, arrayOf(md5)))
    }
    /*@Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(duplicate: DuplicateMediaFile)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(duplicates: List<DuplicateMediaFile>)*/

    /*@Delete
    fun delete(duplicate: DuplicateMediaFile)*/


    /*@Query("DELETE FROM duplicate_files WHERE uriString = :uriString")
    fun deleteByUriString(uriString: String)
    */

    @Delete
    fun delete(duplicate: DuplicateEntity)

    @Query("DELETE FROM duplicates WHERE uriString = :uriString")
    fun deleteByUriString(uriString: String)

    @Delete
    fun deleteAll(duplicates: List<DuplicateEntity>)

    @Query("DELETE FROM duplicates")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(duplicateEntity: DuplicateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(duplicateEntities: List<DuplicateEntity>)

}