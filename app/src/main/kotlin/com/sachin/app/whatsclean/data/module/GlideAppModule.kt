package com.sachin.app.whatsclean.data.module

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule


@GlideModule
class GlideAppModule : AppGlideModule() {

    /*override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)

        registry.prepend(
            MediaFile::class.java,
            InputStream::class.java,
            object : ModelLoaderFactory<MediaFile, InputStream> {
                override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<MediaFile, InputStream> {
                    return PdfModeLoader(context)
                }

                override fun teardown() {

                }

            }
        ).prepend(
            MediaFile::class.java,
            InputStream::class.java,
            object : ModelLoaderFactory<MediaFile, InputStream> {
                override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<MediaFile, InputStream> {
                    return ApkModelLoader(context)
                }

                override fun teardown() {

                }

            }
        ).prepend(
            InputStream::class.java,
            Drawable::class.java,
            ApkDecoder(context)
        )
    }*/
}