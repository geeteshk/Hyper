package io.geeteshk.hyper.util

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
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

fun ViewGroup.inflate(layoutId: Int): View =
        LayoutInflater.from(context).inflate(layoutId, this, false)

fun EditText.string() = text.toString()

fun TextInputEditText.string() = text.toString()

fun <T> MutableLiveData<T>.notify() {
    this.value = this.value
}

fun Spinner.onItemSelected(onItemSelected: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            onItemSelected.invoke(p2)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }
}

fun DrawerLayout.onDrawerOpened(onDrawerOpened: () -> Unit) {
    addDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerOpened(drawerView: View) {
            onDrawerOpened.invoke()
        }

        override fun onDrawerStateChanged(newState: Int) {}
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        override fun onDrawerClosed(drawerView: View) {}
    })
}

fun ViewPropertyAnimator.onAnimationStop(onAnimationStop: () -> Unit) {
    setListener(object : Animator.AnimatorListener {
        override fun onAnimationEnd(p0: Animator?) {
            onAnimationStop.invoke()
        }

        override fun onAnimationCancel(p0: Animator?) {
            onAnimationStop.invoke()
        }

        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationRepeat(p0: Animator?) {}
    })
}

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

fun Context.compatColor(@ColorRes color: Int) =
        ContextCompat.getColor(this, color)

fun Activity.startAndFinish(intent: Intent) {
    startActivity(intent)
    finish()
}

fun String.replace(vararg pairs: Pair<String, String>) =
        pairs.fold(this) { it, (old, new) -> it.replace(old, new, true) }

fun Editable.span(color: Int, range: IntRange) =
        setSpan(ForegroundColorSpan(color), range.start, range.endInclusive, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

fun Editable.color(regex: Regex, color: Int) {
    regex.findAll(this).forEach {
        span(color, it.range)
    }
}

fun ImageView.flip(active: Boolean) {
    animate().rotation((if (active) 0 else -90).toFloat()).duration = 150
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
