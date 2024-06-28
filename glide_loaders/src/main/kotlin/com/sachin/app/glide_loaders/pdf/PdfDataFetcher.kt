package com.sachin.app.glide_loaders.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.applyCanvas
import androidx.core.net.toUri
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PdfDataFetcher(
    private val context: Context,
    private val uriString: String
) : DataFetcher<InputStream> {

    override fun loadData(
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>
    ) {
        try {
            val bitmap = generatePdfThumbnail()
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val bis = ByteArrayInputStream(bos.toByteArray())
            callback.onDataReady(bis)
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    private fun generatePdfThumbnail(): Bitmap {
        val fd = requireNotNull(
            context.contentResolver
                .openFileDescriptor(
                    uriString.toUri(), "r"
                )
        )

        val renderer = PdfRenderer(fd)
        val page = renderer.openPage(0)

        val isPortrait = page.width < page.height

        var width = page.width
        var height = page.height

        val scaleFactor = if (isPortrait) {
            minOf(page.height, MAX_PDF_WIDTH).toFloat() / height.toFloat()
        } else {
            minOf(page.width, MAX_PDF_WIDTH).toFloat() / width.toFloat()
        }

        width = (width * scaleFactor).toInt()
        height = (height * scaleFactor).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.applyCanvas { drawColor(Color.WHITE) }
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    override fun cleanup() {

    }

    override fun cancel() {

    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    companion object {
        private const val MAX_PDF_WIDTH = 300
    }
}
