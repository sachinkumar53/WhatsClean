package com.sachin.app.whatsclean.ui.media

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.*
import com.sachin.app.whatsclean.databinding.MediaGridItemBinding
import com.sachin.app.whatsclean.util.FileSizeFormatter

class MediaAdapter(
    private val viewModel: MediaViewModel
) : ListAdapter<MediaFile, MediaAdapter.MediaViewHolder>(MediaDiffUtilCallback) {

    private var _onItemLongClickListener: OnItemLongClickListener? = null
    private var _onItemClickListener: OnItemClickListener? = null

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener) {
        _onItemLongClickListener = onItemLongClickListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        _onItemClickListener = onItemClickListener
    }

    inner class MediaViewHolder(
        private val binding: MediaGridItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            with(binding) {
                imageViewContainer.setOnClickListener {
                    val bindingPosition = adapterPosition
                    if (bindingPosition != RecyclerView.NO_POSITION) {
                        val file = getItem(bindingPosition)
                        _onItemClickListener?.invoke(
                            binding.imageView,
                            bindingPosition,
                            file
                        )
                    }
                }

                imageViewContainer.setOnLongClickListener {
                    val bindingPosition = adapterPosition
                    if (bindingPosition != RecyclerView.NO_POSITION) {
                        val file = getItem(bindingPosition)
                        _onItemLongClickListener?.invoke(
                            bindingPosition,
                            file
                        )
                    }
                    true
                }
            }
        }

        fun bind(file: MediaFile) = with(binding) {
            //icon.transitionName = "img_${file.uriString}"
            imageView.setImageDrawable(null)
            val isVideoFile = file.mimeType?.startsWith("video") ?: false
            file.loadIcon(imageView)
            val shouldShowName = arrayOf(
                MediaType.DOCUMENT,
                MediaType.AUDIO,
                MediaType.VOICE
            ).any { it == file.mediaType }
            fileName.isVisible = shouldShowName

            if (shouldShowName) {
                fileName.text = file.name
            }

            binding.copyCountTv.apply {
                if (file is DuplicateMediaFile) {
                    isVisible = true
                    text = file.count.toString()
                } else isVisible = false
            }
            sizeTextView.apply {
                text = FileSizeFormatter.format(
                    if (file is DuplicateMediaFile) file.totalLength else file.length
                ).toString()

                updateCompoundDrawables(
                    left = if (isVideoFile) ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_round_play_arrow_24
                    ) else null
                )
            }
            checkbox.isVisible = viewModel.isSelectionMode.value
            val isSelected = viewModel.isMediaFileSelected(file)
            checkbox.isChecked = isSelected
            imageViewContainer.isSelected = isSelected
        }

        /*private suspend fun loadAppIcon(file: MediaFile, binding: MediaGridItemBinding) {
            val context = binding.root.context
            try {
                val appInfo = FileUtil.getApplicationInfoFromFile(context, file)
                val icon = appInfo?.loadIcon(context.packageManager)

                withContext(Dispatchers.Main) {
                    GlideApp.with(context)
                        .load(icon)
                        .placeholder(R.drawable.apk)
                        .into(binding.imageView)
                }
            } catch (e: Exception) {
                Log.e("Sachin", "bind:Error", e)
            }
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MediaViewHolder(
        MediaGridItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private fun TextView.updateCompoundDrawables(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) = setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)


typealias OnItemLongClickListener = (position: Int, file: MediaFile) -> Unit
typealias OnItemClickListener = (imageView: ImageView, position: Int, file: MediaFile) -> Unit

@DrawableRes
fun getIconResIdFromExtension(
    extension: String
): Int = when (extension) {
    "jpg", "jpeg" -> R.drawable.ic_jpg
    "png" -> R.drawable.image
    "gif" -> R.drawable.image
    "txt" -> R.drawable.text
    "xls" -> R.drawable.excel
    "pdf" -> R.drawable.pdf
    "ppt" -> R.drawable.ppt
    "csv" -> R.drawable.ic_csv
    "doc" -> R.drawable.doc
    "apk" -> R.drawable.apk
    "zip" -> R.drawable.zip
    "mp3" -> R.drawable.music
    "wav" -> R.drawable.mp3
    else -> R.drawable.ic_file_default
}
