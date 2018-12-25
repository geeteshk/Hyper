package io.geeteshk.hyper.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun ViewGroup.inflate(layoutId: Int): View =
        LayoutInflater.from(context).inflate(layoutId, this, false)

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit = {}) {
    with (Snackbar.make(this, message, length)) {
        f()
        show()
    }
}

inline fun View.snack(@StringRes message: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit = {}) {
    with (Snackbar.make(this, message, length)) {
        f()
        show()
    }
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}

fun Snackbar.callback(callback: () -> Unit) {
    addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            callback.invoke()
        }
    })
}
