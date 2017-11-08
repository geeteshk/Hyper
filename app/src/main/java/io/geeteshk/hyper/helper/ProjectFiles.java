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

package io.geeteshk.hyper.helper;

public class ProjectFiles {

    static class Default {

        /**
         * HTML BareBones Template
         */
        static final String INDEX = "<!doctype html>\n" +
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
                "</html>";

        /**
         * Empty Style
         */
        static final String STYLE = "/* Add all your styles here */";

        /**
         * Empty Script
         */
        static final String MAIN = "// Add all your JS here";

        static String getIndex(String name, String author, String description, String keywords) {
            return INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords);
        }
    }

    static class Import {

        /**
         * HTML BareBones Template
         */
        static final String INDEX = "<!doctype html>\n" +
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
                "</html>";

        static String getIndex(String name, String author, String description, String keywords) {
            return INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords);
        }
    }
}
