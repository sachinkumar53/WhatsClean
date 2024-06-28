package com.sachin.app.whatsclean.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Button
import android.widget.CheckBox
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.sachin.app.whatsclean.R

class BottomSheetDialogBuilder(
    private val context: Context,
    private val icon: Drawable? = null,
    private val title: CharSequence? = null,
    private val message: CharSequence? = null,
    private val showCheckBox: Boolean = false,
    private val positiveButtonText: String? = null,
    private val negativeButtonText: String? = null,
    private inline val onNegativeButtonClick: (() -> Unit)? = null,
    private inline val onPositiveButtonClick: ((isChecked: Boolean) -> Unit)? = null

) {
    fun build(): BottomSheetDialog {

        return BottomSheetDialog(
            context,
            R.style.Theme_WhatsClean_BottomSheetDialog
        ).apply {
            setContentView(R.layout.dialog_confirmation)
            findViewById<MaterialTextView>(R.id.dialog_title)?.apply {
                setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
                text = title
            }

            findViewById<MaterialTextView>(R.id.dialog_message)?.apply {
                text = message
            }

            val checkBox = findViewById<CheckBox>(R.id.dialog_checkbox)
            checkBox?.isVisible = showCheckBox

            findViewById<Button>(R.id.negative_button)?.apply {
                negativeButtonText?.let { text = it }
                setOnClickListener {
                    onNegativeButtonClick?.invoke()
                    dismiss()
                }
            }

            findViewById<Button>(R.id.positive_button)?.apply {
                positiveButtonText?.let { text = it }
                setOnClickListener {
                    onPositiveButtonClick?.invoke(checkBox?.isChecked == true)
                    dismiss()
                }
            }
        }
    }
}