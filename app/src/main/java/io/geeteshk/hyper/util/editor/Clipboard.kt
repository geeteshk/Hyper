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

import com.unnamed.b.atv.model.TreeNode

import java.io.File

class Clipboard {
    var currentFile: File? = null
    var type = Type.COPY
    var currentNode: TreeNode? = null

    enum class Type {
        COPY, CUT
    }

    companion object {

        val instance = Clipboard()
    }
}
