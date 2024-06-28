package com.sachin.app.whatsclean.ui.gallery

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.sachin.app.whatsclean.data.model.MediaDiffUtilCallback
import com.sachin.app.whatsclean.data.model.MediaFile

class GalleryAdapter(
    private inline val onClick: () -> Unit
) : ListAdapter<MediaFile, GalleryAdapter.GalleryViewHolder>(MediaDiffUtilCallback) {

    inner class GalleryViewHolder(
        private val photoView: ImageView
    ) : RecyclerView.ViewHolder(photoView) {

        init {
            photoView.setOnClickListener { onClick() }
        }

        fun bind(uri: String) {
            Glide.with(photoView)
                .load(uri)
                .into(photoView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(PhotoView(parent.context).apply {
            fitsSystemWindows = true
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        })
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(getItem(position).uriString)
    }
}