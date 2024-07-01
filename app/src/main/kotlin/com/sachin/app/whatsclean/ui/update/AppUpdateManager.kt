package com.sachin.app.whatsclean.ui.update

import android.util.Log
import com.sachin.app.whatsclean.BuildConfig
import com.sachin.app.whatsclean.data.network.GithubApiClient
import kotlinx.coroutines.CancellationException

private const val TAG = "AppUpdateManager"

class AppUpdateManager {


    suspend fun isAppUpdateAvailable(): Boolean {
        val currentVersion = getCurrentVersion()
        val latestVersion = getLatestVersionFromGithub()
        try {
            val currentVersionCode = currentVersion.filter { it.isDigit() }.toInt()
            val latestVersionCode = latestVersion.filter { it.isDigit() }.toInt()
            Log.i(TAG, "isAppUpdateAvailable: $currentVersionCode and $latestVersionCode")
            return currentVersionCode < latestVersionCode
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "isAppUpdateAvailable: ", e)
            return false
        }
    }

    private fun getCurrentVersion(): String {
        return BuildConfig.VERSION_NAME
    }

    private suspend fun getLatestVersionFromGithub(): String {
        return GithubApiClient.getLatestVersionFromGithub()

    }

}