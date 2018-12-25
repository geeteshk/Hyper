/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.util.editor

import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.geeteshk.hyper.extensions.snack
import java.io.File

object Clipboard {

    var currentFile: File? = null
    var type = Type.COPY

    fun update(file: File, t: Type, view: View) {
        currentFile = file
        type = t

        val msg = if (t == Type.COPY) {
            "copied"
        } else {
            "moved"
        }

        view.snack("${file.name} selected to be $msg.", Snackbar.LENGTH_SHORT)
    }

    enum class Type {
        COPY, CUT
    }
}
