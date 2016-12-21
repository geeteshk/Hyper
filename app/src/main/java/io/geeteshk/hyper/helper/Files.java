package io.geeteshk.hyper.helper;

public class Files {

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
