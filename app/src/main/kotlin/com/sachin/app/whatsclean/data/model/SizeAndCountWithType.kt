package com.sachin.app.whatsclean.data.model

import androidx.room.Embedded

data class SizeAndCountWithType(
    val mediaType: MediaType,
    @Embedded
    val sizeAndCount: SizeAndCount,
)
