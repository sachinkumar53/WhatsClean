package com.sachin.lib.apkdecoder

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource

class ApplicationIconDecoder(
    private val packageManager: PackageManager
) : ResourceDecoder<ApplicationInfo, Drawable> {

    override fun handles(source: ApplicationInfo, options: Options): Boolean {
        return true
    }

    override fun decode(
        source: ApplicationInfo,
        width: Int,
        height: Int,
        options: Options
    ): Resource<Drawable>? {
        val icon = packageManager.getApplicationIcon(source)
        return ApplicationIconResource(icon)
    }


}