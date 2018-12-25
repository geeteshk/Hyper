package io.geeteshk.hyper.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.compatColor(@ColorRes color: Int) =
        ContextCompat.getColor(this, color)

fun Activity.startAndFinish(intent: Intent) {
    startActivity(intent)
    finish()
}