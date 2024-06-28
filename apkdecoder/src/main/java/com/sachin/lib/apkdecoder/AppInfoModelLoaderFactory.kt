package com.sachin.lib.apkdecoder

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class AppInfoModelLoaderFactory(
    private val packageManager: PackageManager
) : ModelLoaderFactory<ApplicationInfo, Drawable> {

    override fun build(
        multiFactory: MultiModelLoaderFactory
    ): ModelLoader<ApplicationInfo, Drawable> {
        return AppInfoModelLoader(packageManager)
    }

    override fun teardown() {

    }
}