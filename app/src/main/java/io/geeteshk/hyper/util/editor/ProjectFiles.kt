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

import android.content.Context

import io.geeteshk.hyper.util.replace

class ProjectFiles {

    companion object {

        fun readTextFromAsset(context: Context, name: String) =
                context.assets.open(name).bufferedReader().use { it.readText() }

        fun getHtml(context: Context, type: String, name: String, author: String, description: String, keywords: String) =
                readTextFromAsset(context, "files/$type/index.html")
                        .replace(
                                "@name" to  name,
                                "@author" to author,
                                "@description" to description,
                                "@keywords" to keywords)

        fun getCss(context: Context, type: String) =
                readTextFromAsset(context, "files/$type/style.css")

        fun getJs(context: Context, type: String) =
                readTextFromAsset(context, "files/$type/main.js")
    }
}
