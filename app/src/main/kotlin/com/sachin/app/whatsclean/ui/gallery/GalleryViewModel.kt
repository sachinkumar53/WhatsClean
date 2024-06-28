package com.sachin.app.whatsclean.ui.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.*
import com.sachin.app.whatsclean.data.repositories.MediaRepository
import com.sachin.app.whatsclean.util.BottomSheetDialogBuilder
import com.sachin.app.whatsclean.util.FileUtil
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import com.sachin.app.whatsclean.util.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var isLightNav = false

    var position: Int = savedStateHandle["initial_position"] ?: 0
    private val mediaType: MediaType = savedStateHandle["mediaType"]!!
    private val gridType: GridType = savedStateHandle["gridType"]!!
    //val files = (savedStateHandle.get<Array<MediaFile>>("file_list"))?.toMutableList()

    val imageFilesFlow = repository.getImageFilesFlowByType(gridType, mediaType)

    private fun shareFile(context: Context, file: MediaFile) = viewModelScope.launch {
        FileUtil.startFileShareIntent(context, listOf(file))
    }

    var currentFile: MediaFile? = null

    fun onBottomMenuItemSelected(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        itemId: Int,
        onSuccess: () -> Unit
    ): Boolean = when (itemId) {

        R.id.action_delete -> {
            currentFile?.let {
                deleteFile(activity, it, onSuccess)
                currentFile = null
            }
            true
        }

        R.id.action_share -> {
            currentFile?.let {
                shareFile(activity, it)
                currentFile = null
            }
            true
        }
        R.id.action_move -> {
            currentFile?.let {
                moveFile(activity, launcher, it, onSuccess)
            }
            true
        }

        else -> false
    }

    private fun deleteFile(
        context: Context,
        file: MediaFile,
        onSuccess: () -> Unit
    ) {
        if (gridType == GridType.DUPLICATES) {
            BottomSheetDialogBuilder(
                context = context,
                icon = ContextCompat.getDrawable(context, R.drawable.ic_round_delete_24),
                title = context.getString(R.string.delete_dialog_title),
                message = context.getString(R.string.delete_dialog_message),
                showCheckBox = true
            ) { isChecked ->
                file as DuplicateMediaFile
                if (isChecked)
                    repository.deleteCopies(listOf(file)) { isSuccess ->
                        onCompleteEvent(isSuccess, file, onSuccess)
                        context.showToast(
                            if (isSuccess) "All copies deleted successfully!"
                            else "Failed to delete some files"
                        )
                    }
                else repository.deleteFiles(listOf(file)) { isSuccess ->
                    onCompleteEvent(isSuccess, file, onSuccess)
                    context.showToast(
                        if (isSuccess) "All files deleted successfully!"
                        else "Failed to delete some files"
                    )
                }

            }.build()
        } else {
            BottomSheetDialogBuilder(
                context = context,
                icon = ContextCompat.getDrawable(context, R.drawable.ic_round_delete_24),
                title = context.getString(R.string.delete_dialog_title),
                message = context.getString(R.string.delete_dialog_message)
            ) {
                repository.deleteFiles(
                    listOf(file as SingleMediaFile)
                ) { isSuccess ->
                    onCompleteEvent(isSuccess, file, onSuccess)
                    context.showToast(
                        if (isSuccess) "File deleted successfully!"
                        else "Failed to delete file"
                    )
                }

            }.build()
        }.show()
    }

    private inline fun onCompleteEvent(
        isSuccess: Boolean,
        file: MediaFile,
        onSuccess: () -> Unit
    ) {
        if (isSuccess) {
            //files?.remove(file)
            currentFile = null
            onSuccess()
        }
    }


    private fun moveFile(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
        file: MediaFile,
        onSuccess: () -> Unit
    ) {
        if (isAndroidRorAbove) {
            currentFile = file
            BottomSheetDialogBuilder(
                context = context,
                icon = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_round_drive_file_move_24
                ),
                title = context.getString(R.string.move_destination_dialog_title),
                message = context.getString(R.string.move_destination_dialog_message),
                positiveButtonText = context.getString(R.string.choose)
            ) {
                val sm = context.getSystemService(StorageManager::class.java)
                launcher.launch(
                    sm.primaryStorageVolume.createOpenDocumentTreeIntent()
                )
            }.build().show()
        } else {
            BottomSheetDialogBuilder(
                context = context,
                icon = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_round_drive_file_move_24
                ),
                title = context.getString(R.string.move_confirmation_dialog_title),
                message = context.getString(R.string.move_confirmation_dialog_message),
                positiveButtonText = context.getString(R.string.yes),
                negativeButtonText = context.getString(R.string.no)
            ) {
                repository.moveFiles(listOf(file as SingleMediaFile)) { isSuccess ->
                    onCompleteEvent(isSuccess, file, onSuccess)

                    context.showToast(
                        if (isSuccess)
                            "File moved to gallery"
                        else "Failed to move media"
                    )
                }
            }.build().show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun moveFile(
        context: Context,
        destinationUri: Uri,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        currentFile?.let {
            it as SingleMediaFile
            repository.moveFiles(listOf(it), destinationUri) { isSuccess ->
                onCompleteEvent(isSuccess, it, onSuccess)
                context.showToast(
                    if (isSuccess)
                        "File moved to selected folder"
                    else "Failed to move media"
                )
            }
        }
    }

}