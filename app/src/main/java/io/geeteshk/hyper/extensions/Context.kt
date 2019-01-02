package io.geeteshk.hyper.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.io.Serializable

fun Context.compatColor(@ColorRes color: Int) =
        ContextCompat.getColor(this, color)

fun Activity.startAndFinish(intent: Intent) {
    startActivity(intent)
    finish()
}

inline fun <reified T> Context.intentFor(vararg extras: Pair<String, Serializable>) =
        Intent(this, T::class.java).apply {
            extras.forEach { putExtra(it.first, it.second) }
        }

inline fun <reified T> Context.startActivity(vararg extras: Pair<String, Serializable>) =
        startActivity(intentFor<T>(*extras))

inline fun <reified T> Activity.startActivityForResult(code: Int, vararg extras: Pair<String, Serializable>) =
        startActivityForResult(intentFor<T>(*extras), code)

fun Intent.withFlags(vararg flags: Int) = this.apply {
    flags.forEach { addFlags(it) }
}