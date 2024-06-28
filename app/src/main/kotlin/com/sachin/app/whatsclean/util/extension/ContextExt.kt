package com.sachin.app.whatsclean.util.extension

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.sachin.app.whatsclean.BuildConfig
import java.io.File


fun Context.checkStoragePermission(): Boolean {
    return if (isAndroidRorAbove) {
        val uris = contentResolver.persistedUriPermissions
        return if (uris.isNotEmpty()) {
            return uris.all { it?.uri?.isValidateWhatsAppTreeUri() == true }
        } else false
    } else {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

inline val isAndroidRorAbove: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

val Context.externalStorageDir: File?
    get() = getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile


fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Context.showToast(@StringRes textResId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, textResId, duration).show()

fun Context.sendEmail(
    subject: String? = "[FEEDBACK] WhatsClean | App by Sachin",
    text: String? = "Dear developer!\n"
) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("feedbacks.sachin@gmail.com"))

    if (subject != null)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)

    if (text !== null)
        intent.putExtra(Intent.EXTRA_TEXT, text)

    try {
        startActivity(Intent.createChooser(intent, "Send email using"))
    } catch (ex: ActivityNotFoundException) {
        showToast("There are no email clients installed.")
    }
}

fun Context.openPlayStore() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    }
}

fun Context.openDeveloperPage() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/dev?id=6475482614401654947")
            )
        )
    } catch (e: ActivityNotFoundException) {
        showToast("No application found to handle this action")
    }
}

fun Context.sendAppLink() {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}


fun Context.dpTpPx(value: Float) = value * resources.displayMetrics.density


/**
 * Retrieve a color from the current [android.content.res.Resources.Theme].
 */
@ColorInt
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}