package io.geeteshk.hyper.extensions

import android.view.View
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.io.File
import java.io.InputStream

fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}

fun File.copy(newFile: File, view: View): Boolean {
    return try {
        copyRecursively(newFile)
    } catch (e: Exception) {
        Timber.e(e)
        view.snack(e.toString(), Snackbar.LENGTH_SHORT)
        false
    }
}

fun File.move(newFile: File, view: View): Boolean {
    return try {
        copyRecursively(newFile)
        deleteRecursively()
    } catch (e: Exception) {
        Timber.e(e)
        view.snack(e.toString(), Snackbar.LENGTH_SHORT)
        false
    }
}
