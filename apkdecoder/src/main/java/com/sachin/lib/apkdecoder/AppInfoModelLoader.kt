package com.sachin.lib.apkdecoder

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey

class AppInfoModelLoader(
    private val packageManager: PackageManager
) : ModelLoader<ApplicationInfo, Drawable> {

    override fun buildLoadData(
        model: ApplicationInfo,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<Drawable> {
        return ModelLoader.LoadData(
            ObjectKey(model),
            AppIconFetcher(packageManager, model)
        )
    }

    override fun handles(model: ApplicationInfo): Boolean = true
}