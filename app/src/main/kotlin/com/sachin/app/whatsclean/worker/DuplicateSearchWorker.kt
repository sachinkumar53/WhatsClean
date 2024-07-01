package com.sachin.app.whatsclean.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DuplicateSearchWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result {
        // TODO: Implement the duplicate search logic here
        return Result.success()
    }

    private suspend fun generateAndSaveMd5() = withContext(Dispatchers.IO) {
        // TODO: Implement the MD5 generation logic here
    }

}