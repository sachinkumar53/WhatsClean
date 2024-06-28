package com.sachin.app.whatsclean.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duplicates")
data class DuplicateEntity(
    @PrimaryKey(autoGenerate = false)
    val uriString: String,
    val mediaType: MediaType,
    val md5String: String?
)