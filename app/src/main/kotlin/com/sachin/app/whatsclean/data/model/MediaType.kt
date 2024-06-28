package com.sachin.app.whatsclean.data.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.sachin.app.whatsclean.R

enum class MediaType(
    val path: String,
    val privateDir: String? = "$path/Private",
    val sentDir: String? = "$path/Sent"
) {
    IMAGE("Media/WhatsApp Images"),
    ANIMATED_GIF("Media/WhatsApp Animated Gifs"),
    AUDIO("Media/WhatsApp Audio") {
        override val defaultIconResId = R.drawable.music
    },
    VIDEO("Media/WhatsApp Video"),
    DOCUMENT("Media/WhatsApp Documents"),
    WALLPAPER("Media/WallPaper", null, null),
    VOICE("Media/WhatsApp Voice Notes", null, null) {
        override val defaultIconResId = R.drawable.record
    },
    STATUS("Media/.Statuses", null, null),
    STICKER("Media/WhatsApp Stickers", null, null),
    PROFILE_PHOTO("Media/WhatsApp Profile Photos", null, null),
    DATABASE("Databases", null, null) {
        override val defaultIconResId = R.drawable.ic_database
    };

    @DrawableRes
    open val defaultIconResId: Int? = null

    fun getDefaultIcon(context: Context): Drawable? = defaultIconResId?.let {
        ContextCompat.getDrawable(context, it)
    }
}

val MediaType.titleResId: Int
    get() = when (this) {
        MediaType.IMAGE -> R.string.image
        MediaType.DOCUMENT -> R.string.document
        MediaType.DATABASE -> R.string.database
        MediaType.PROFILE_PHOTO -> R.string.profile_photo
        MediaType.STICKER -> R.string.sticker
        MediaType.VOICE -> R.string.voice_notes
        MediaType.ANIMATED_GIF -> R.string.animated_gif
        MediaType.AUDIO -> R.string.audio
        MediaType.VIDEO -> R.string.video
        MediaType.STATUS -> R.string.status
        MediaType.WALLPAPER -> R.string.wallpaper
    }

fun MediaType.getTitle(context: Context): String = context.getString(titleResId)
