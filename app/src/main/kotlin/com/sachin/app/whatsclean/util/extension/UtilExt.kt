package com.sachin.app.whatsclean.util.extension

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend inline fun <T> ioDispatcher(
    crossinline block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.IO) {
    block()
}

inline fun <P, R> CoroutineScope.executeAsyncTask(
    crossinline onPreExecute: () -> Unit = {},
    crossinline doInBackground: suspend (suspend (P) -> Unit) -> R,
    crossinline onPostExecute: (R) -> Unit = {},
    crossinline onProgressUpdate: (P) -> Unit = {}
) = launch {
    onPreExecute()
    val result = ioDispatcher {
        doInBackground {
            withContext(Dispatchers.Main) {
                onProgressUpdate(it)
            }
        }
    }
    onPostExecute(result)
}

fun Uri?.isValidateWhatsAppTreeUri(): Boolean {
    return if (this == null) false
    else arrayOf(WHATSAPP_URI, WHATSAPP_BUSINESS_URI).any { validUri ->
        toString().endsWith(validUri)
    }
}

const val WHATSAPP_URI = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp"
const val WHATSAPP_BUSINESS_URI = "Android%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business"