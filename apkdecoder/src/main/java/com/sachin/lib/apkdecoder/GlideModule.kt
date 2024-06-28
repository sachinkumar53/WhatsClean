package com.sachin.lib.apkdecoder

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule


@GlideModule
class GlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            ApplicationInfo::class.java,
            Drawable::class.java,
            AppInfoModelLoaderFactory(context.packageManager)
        )

        registry.append(
            ApplicationInfo::class.java,
            Drawable::class.java,
            ApplicationIconDecoder(context.packageManager)
        )
    }

}