package com.sachin.app.glide_loaders.pdf

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class PdfModeLoader(
    private val context: Context
) : ModelLoader<String, InputStream> {
    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val key = "${model}:$width$height"
        return ModelLoader.LoadData(
            ObjectKey(key),
            PdfDataFetcher(context, model)
        )
    }

    override fun handles(model: String): Boolean {
        return model.endsWith("pdf")
    }
}