package com.sachin.app.whatsclean

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.util.extension.openDeveloperPage
import com.sachin.app.whatsclean.util.extension.sendAppLink
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val theme = settingsManager.getTheme().asLiveData()

    fun onNavigationItemSelected(
        context: Context,
        navController: NavController,
        itemId: Int
    ): Boolean = when (itemId) {
        R.id.settingsFragment -> {
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            navController.navigate(itemId, null, options)
            true
        }

        /*R.id.rate -> {
            context.openPlayStore()
            true
        }*/

        R.id.share -> {
            context.sendAppLink()
            true
        }

        R.id.more_apps -> {
            context.openDeveloperPage()
            true
        }

        else -> false
    }

    fun needsAuthentication(context: Context): Boolean {
        val isFingerPrintEnabled = runBlocking { settingsManager.getFingerprint().first() }
        val canAuthenticate = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && isFingerPrintEnabled
    }

    fun setAppTheme(theme: String) {
        when (theme) {
            "light" -> {
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            "dark" -> {
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            "follow_system" -> {
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            "battery_saver" -> {
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }


    }
}

private const val TAG = "MainViewModel"