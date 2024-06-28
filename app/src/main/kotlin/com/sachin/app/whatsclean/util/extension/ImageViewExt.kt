package com.sachin.app.whatsclean.util.extension

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop

fun ImageView.load(
    data: String?,
    size: Int = 200,
    placeholder: Drawable? = null
) {
    Glide.with(this)
        .load(data)
        .placeholder(placeholder)
        .override(size)
        .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(CenterCrop()))
        .into(this)
}

fun ImageView.load(
    data: String,
    size: Int = 200,
    @DrawableRes
    placeholder: Int? = null
) {
    if (placeholder != null) {
        Glide.with(this)
            .load(Uri.parse(data))
            .placeholder(placeholder)
            .thumbnail(0.5f)
            .override(size)
            .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(CenterCrop()))
            .into(this)
    } else {
        Glide.with(this)
            .load(Uri.parse(data))
            .thumbnail(0.5f)
            .override(size)
            .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(CenterCrop()))
            .into(this)
    }
}