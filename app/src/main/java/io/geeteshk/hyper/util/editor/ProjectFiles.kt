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

internal class ProjectFiles {

    internal object Default {

        private val INDEX = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>@name</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <meta name=\"author\" content=\"@author\">\n" +
                "    <meta name=\"description\" content=\"@description\">\n" +
                "    <meta name=\"keywords\" content=\"@keywords\">\n" +
                "    <link rel=\"shortcut icon\" href=\"images/favicon.ico\" type=\"image/vnd.microsoft.icon\">\n" +
                "    <link rel=\"stylesheet\" href=\"css/style.css\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Hello World!</h1>\n" +
                "    <script src=\"js/main.js\"></script>\n" +
                "  </body>\n" +
                "</html>"

        val STYLE = "/* Add all your styles here */"

        val MAIN = "// Add all your JS here"

        fun getIndex(name: String, author: String, description: String, keywords: String): String =
                INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords)
    }

    internal object Import {

        private val INDEX = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>@name</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <meta name=\"author\" content=\"@author\">\n" +
                "    <meta name=\"description\" content=\"@description\">\n" +
                "    <meta name=\"keywords\" content=\"@keywords\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Hello World!</h1>\n" +
                "  </body>\n" +
                "</html>"

        fun getIndex(name: String, author: String, description: String, keywords: String): String =
                INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords)
    }
}
