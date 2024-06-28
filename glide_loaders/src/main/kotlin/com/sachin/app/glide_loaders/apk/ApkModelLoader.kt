package com.sachin.app.glide_loaders.apk

import android.content.Context
import android.util.Size
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class ApkModelLoader(
    private val context: Context
) : ModelLoader<String, InputStream> {
    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {

        return ModelLoader.LoadData(
            ObjectKey(model),
            ApkDataFetcher(context, model, Size(width, height))
        )
    }

    override fun handles(model: String): Boolean {
        return model.endsWith("apk")
    }
}