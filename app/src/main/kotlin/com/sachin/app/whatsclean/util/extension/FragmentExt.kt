package com.sachin.app.whatsclean.util.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment

fun Fragment.startSupportActionMode(
    callback: ActionMode.Callback
) = (requireActivity() as AppCompatActivity).startSupportActionMode(callback)
