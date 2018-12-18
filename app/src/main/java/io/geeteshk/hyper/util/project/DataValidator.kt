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

package io.geeteshk.hyper.util.project

import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.geeteshk.hyper.R
import java.util.*

object DataValidator {

    fun validateCreate(context: Context, name: TextInputLayout, author: TextInputLayout, description: TextInputLayout, keywords: TextInputLayout): Boolean {
        if (name.editText!!.text.toString().isEmpty()) {
            name.error = context.getString(R.string.name_error)
            return false
        }

        if (author.editText!!.text.toString().isEmpty()) {
            author.error = context.getString(R.string.author_error)
            return false
        }

        if (description.editText!!.text.toString().isEmpty()) {
            description.error = context.getString(R.string.desc_error)
            return false
        }

        if (keywords.editText!!.text.toString().isEmpty()) {
            keywords.error = context.getString(R.string.keywords_error)
            return false
        }

        return true
    }

    fun validateClone(context: Context, name: TextInputEditText, remote: TextInputEditText): Boolean {
        if (name.text.toString().isEmpty()) {
            name.error = context.getString(R.string.name_error)
            return false
        }

        if (remote.text.toString().isEmpty()) {
            remote.error = context.getString(R.string.remote_error)
            return false
        }

        return true
    }

    fun removeBroken(objectsList: ArrayList<*>) {
        val iterator = objectsList.iterator()
        while (iterator.hasNext()) {
            val string = iterator.next() as String
            if (!ProjectManager.isValid(string)) {
                iterator.remove()
            }
        }
    }
}
