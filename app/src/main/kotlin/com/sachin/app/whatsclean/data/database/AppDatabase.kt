package com.sachin.app.whatsclean.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sachin.app.whatsclean.data.model.DuplicateEntity
import com.sachin.app.whatsclean.data.model.SingleMediaFile


@Database(entities = [SingleMediaFile::class, DuplicateEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val mediaFilesDao: MediaFilesDao
    abstract val duplicateFilesDao: DuplicateFilesDao
}