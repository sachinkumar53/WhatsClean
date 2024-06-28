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

        return Result.success()
    }

    private suspend fun generateAndSaveMd5() = withContext(Dispatchers.IO) {

    }

}