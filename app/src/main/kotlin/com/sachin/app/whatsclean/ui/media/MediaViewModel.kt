package com.sachin.app.whatsclean.ui.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.sachin.app.whatsclean.MainActivity
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.*
import com.sachin.app.whatsclean.data.repositories.MediaRepository
import com.sachin.app.whatsclean.util.BottomSheetDialogBuilder
import com.sachin.app.whatsclean.util.FileUtil
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import com.sachin.app.whatsclean.util.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mediaType: MediaType = savedStateHandle["media_type"]!!

    fun getMediaFilesFlowByType(gridType: GridType) = repository.getMediaFilesOfType(
        gridType,
        mediaType
    ).asLiveData()


    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectAllFlow = MutableStateFlow(false)
    val selectAllFlow = _selectAllFlow.asStateFlow()

    var mediaAdapter: MediaAdapter? = null

    fun onItemLongClicked(
        position: Int,
        file: MediaFile,
        adapter: MediaAdapter,
        touchListener: DragSelectTouchListener
    ) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
            mediaAdapter = adapter
            selectItem(file, adapter, true)
            touchListener.setIsActive(true, position)
        } else {
            touchListener.setIsActive(true, position)
        }
        adapter.notifyDataSetChanged()
    }

    private fun dismissSelectionMode(adapter: MediaAdapter) {
        _isSelectionMode.value = false
        _selectionList.clear()
        _selectedCountFlow.value = -1
        adapter.notifyDataSetChanged()
    }

    private val _selectionList = mutableListOf<String>()
    private val _selectedCountFlow = MutableStateFlow(-1)
    val selectedCountFlow = _selectedCountFlow.asStateFlow()

    fun onItemClicked(
        context: Context,
        file: MediaFile,
        adapter: MediaAdapter,
        navController: NavController,
        gridType: GridType
    ) {
        if (isSelectionMode.value) {
            selectItem(file, adapter, !_selectionList.contains(file.uriString))
        } else {
            if (file.mimeType?.startsWith("image") == true) {

                val index = adapter.currentList.filter {
                    it.mimeType?.startsWith("image") == true
                }.indexOf(file)

                navController.navigate(
                    MediaFragmentDirections.actionMediaFragmentToGalleryFragment(
                        initialPosition = index,
                        gridType = gridType,
                        mediaType = mediaType
                    )
                )//, extras
                //)
            } else {
                FileUtil.openFile(context, file)
            }
        }
    }

    fun selectItem(file: MediaFile, adapter: MediaAdapter, select: Boolean) {
        if (select && !_selectionList.contains(file.uriString)) {
            _selectionList.add(file.uriString)
            _selectedCountFlow.value = _selectionList.size
            adapter.notifyItemChanged(adapter.currentList.indexOf(file))
            _selectAllFlow.value = adapter.currentList.size == _selectionList.size
        } else if (!select && _selectionList.contains(file.uriString)) {
            _selectionList.remove(file.uriString)
            _selectedCountFlow.value = _selectionList.size
            adapter.notifyItemChanged(adapter.currentList.indexOf(file))
            _selectAllFlow.value = adapter.currentList.size == _selectionList.size
        }
    }

    fun isMediaFileSelected(file: MediaFile): Boolean = _selectionList.contains(file.uriString)

    fun onSelectAllChanged(checked: Boolean) {
        viewModelScope.launch {
            // Remove previous entries
            val adapter = mediaAdapter ?: return@launch
            withContext(Dispatchers.IO) {
                _selectionList.clear()
                if (checked) {
                    _selectionList.addAll(adapter.currentList.map {
                        it.uriString
                    })
                }
            }
            adapter.notifyDataSetChanged()
            _selectAllFlow.value = checked
            _selectedCountFlow.value = _selectionList.size
        }
    }

    fun onDestroyActionMode() {
        mediaAdapter?.let { dismissSelectionMode(it) }
        mediaAdapter = null
    }

    private suspend fun getSelectedFiles(): List<MediaFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<MediaFile>()
        if (_selectAllFlow.value) {
            mediaAdapter?.currentList?.let {
                files.addAll(it)
            }
        } else {
            _selectionList.mapNotNullTo(files) { uri ->
                mediaAdapter?.currentList?.find {
                    it.uriString == uri
                }
            }
        }
        files
    }

    fun onBottomMenuItemSelected(
        activity: MainActivity,
        launcher: ActivityResultLauncher<Intent>,
        actionMode: ActionMode?,
        gridType: GridType,
        itemId: Int
    ): Boolean = when (itemId) {

        R.id.action_delete -> {
            deleteSelectedFiles(activity, actionMode, gridType)
            true
        }

        R.id.action_share -> {
            viewModelScope.launch {
                getSelectedFiles().also {
                    FileUtil.startFileShareIntent(activity, it)
                }
                actionMode?.finish()
            }
            true
        }
        R.id.action_move -> {
            if (isAndroidRorAbove) {
                BottomSheetDialogBuilder(
                    context = activity,
                    icon = ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_round_drive_file_move_24
                    ),
                    title = activity.getString(R.string.move_destination_dialog_title),
                    message = activity.getString(R.string.move_destination_dialog_message),
                    positiveButtonText = activity.getString(R.string.choose)
                ) {
                    val sm = activity.getSystemService(StorageManager::class.java)
                    launcher.launch(
                        sm.primaryStorageVolume.createOpenDocumentTreeIntent()
                    )
                }.build().show()
            } else {
                BottomSheetDialogBuilder(
                    context = activity,
                    icon = ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_round_drive_file_move_24
                    ),
                    title = activity.getString(R.string.move_confirmation_dialog_title),
                    message = activity.getString(R.string.move_confirmation_dialog_message),
                    positiveButtonText = activity.getString(R.string.yes),
                    negativeButtonText = activity.getString(R.string.no)
                ) {
                    viewModelScope.launch {
                        getSelectedFiles().map {
                            it as SingleMediaFile
                        }.also {

                            repository.moveFiles(it) { isSuccess ->

                            }
                        }
                        actionMode?.finish()
                    }
                }.build().show()
            }
            true
        }

        else -> false
    }

    private fun deleteSelectedFiles(
        activity: MainActivity,
        actionMode: ActionMode?,
        gridType: GridType
    ) {
        if (gridType == GridType.DUPLICATES) {
            BottomSheetDialogBuilder(
                context = activity,
                icon = ContextCompat.getDrawable(activity, R.drawable.ic_round_delete_24),
                title = activity.getString(R.string.delete_dialog_title),
                message = activity.getString(R.string.delete_dialog_message),
                showCheckBox = true
            ) { isChecked ->
                viewModelScope.launch {
                    getSelectedFiles().map {
                        it as DuplicateMediaFile
                    }.also {
                        if (isChecked) repository.deleteCopies(it)
                        else repository.deleteFiles(it) { isSuccess ->
                            activity.showToast(
                                if (isSuccess) "All files deleted successfully!"
                                else "Failed to delete some files"
                            )
                        }
                    }
                    actionMode?.finish()
                }
            }.build()
        } else {
            BottomSheetDialogBuilder(
                context = activity,
                icon = ContextCompat.getDrawable(activity, R.drawable.ic_round_delete_24),
                title = activity.getString(R.string.delete_dialog_title),
                message = activity.getString(R.string.delete_dialog_message)
            ) {
                viewModelScope.launch {
                    getSelectedFiles().let {
                        repository.deleteFiles(it) { isSuccess ->
                            activity.showToast(
                                if (isSuccess) "All files deleted successfully!"
                                else "Failed to delete some files"
                            )
                        }
                    }
                    actionMode?.finish()
                }
            }.build()
        }.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun moveSelectedFiles(
        activity: MainActivity,
        destinationUri: Uri,
        actionMode: ActionMode?
    ) = viewModelScope.launch {
        getSelectedFiles().map {
            it as SingleMediaFile
        }.also {
            repository.moveFiles(it, destinationUri) { isSuccess ->
                // TODO:
            }
        }
        actionMode?.finish()
    }

}

private const val TAG = "MediaViewModel"
