package io.geeteshk.hyper.extensions

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.notify() {
    this.value = this.value
}