package com.sachin.lib.apkdecoder

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.bumptech.glide.util.Util

class ApplicationIconResource(
    drawable: Drawable
) : DrawableResource<Drawable>(drawable) {

    override fun getResourceClass(): Class<Drawable> = Drawable::class.java

    override fun getSize(): Int {
        return if (drawable is BitmapDrawable) {
            Util.getBitmapByteSize(drawable.bitmap)
        } else 1
    }

    override fun recycle() {
        /* not from our pool */
    }
}