package com.sachin.app.whatsclean.util.extension

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment


fun AppCompatActivity.findNavHostFragment(@IdRes id: Int): NavHostFragment {
    return supportFragmentManager.findFragmentById(id) as NavHostFragment
}
