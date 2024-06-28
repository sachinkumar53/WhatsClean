package com.sachin.app.whatsclean.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.data.model.SortType
import com.sachin.app.whatsclean.util.FileUtil
import com.sachin.app.whatsclean.util.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val whatsAppFolderLiveData = settingsManager.getWhatsAppDirectory().asLiveData()
    val treeUriLiveData = settingsManager.getTreeUri().asLiveData()
    val sortTypeLiveData = settingsManager.getSortType().asLiveData()

    fun canAuthenticate(context: Context): Int {
        return BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)
    }

    fun setFingerPrintEnabled(isEnabled: Boolean) {
        settingsManager.setFingerPrint(isEnabled)
    }

    @Suppress("DEPRECATION")
    fun launchFingerprintSetup(context: Context) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Settings.ACTION_BIOMETRIC_ENROLL
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                Settings.ACTION_FINGERPRINT_ENROLL
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                Settings.ACTION_SECURITY_SETTINGS
            }

            else -> null
        }?.also { action ->
            try {
                val intent = Intent(action)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        context.showToast("Can't open settings")
                    }
                }
            }
        }
    }

    fun setSortType(sortType: SortType) {
        settingsManager.setSortType(sortType)
    }

    fun setTreeUri(treeUri: Uri) {
        settingsManager.setTreeUri(treeUri)
    }

    fun setWhatsAppFolder(context: Context, whatsappPath: String) {
        if (FileUtil.validateWhatsAppFolder(whatsappPath))
            settingsManager.setWhatsAppDirectory(whatsappPath)
        else context.showToast(R.string.invalid_whatsapp_folder)
    }

    fun setTheme(theme: String) {
        settingsManager.setTheme(theme)
    }
}