package io.geeteshk.hyper.extensions

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan

fun String.replace(vararg pairs: Pair<String, String>) =
        pairs.fold(this) { it, (old, new) -> it.replace(old, new, true) }

fun Editable.span(color: Int, range: IntRange) =
        setSpan(ForegroundColorSpan(color), range.start, range.endInclusive, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

fun Editable.color(regex: Regex, color: Int) {
    regex.findAll(this).forEach {
        span(color, it.range)
    }
}