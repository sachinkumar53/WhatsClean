package com.sachin.app.glide_loaders

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.LibraryGlideModule
import com.sachin.app.glide_loaders.apk.ApkModelLoader
import com.sachin.app.glide_loaders.pdf.PdfModeLoader
import java.io.InputStream

@GlideModule
class MyGlideModule : LibraryGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.prepend(
            String::class.java,
            InputStream::class.java,
            object : ModelLoaderFactory<String, InputStream> {
                override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
                    return PdfModeLoader(context)
                }

                override fun teardown() {

                }

            }
        ).prepend(
            String::class.java,
            InputStream::class.java,
            object : ModelLoaderFactory<String, InputStream> {
                override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
                    return ApkModelLoader(context)
                }

                override fun teardown() {

                }

            }
        )
    }
}