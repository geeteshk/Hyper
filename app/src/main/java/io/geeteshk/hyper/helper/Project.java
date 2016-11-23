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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;

/**
 * Helper class to handle all project related tasks
 */
public class Project {

    /**
     * HTML BareBones Template
     */
    public static final String INDEX = "<!doctype html>\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>@name</title>\n" +
            "    <meta name=\"author\" content=\"@author\">\n" +
            "    <meta name=\"description\" content=\"@description\">\n" +
            "    <meta name=\"keywords\" content=\"@keywords\">\n" +
            "    <link rel=\"shortcut icon\" href=\"images/favicon.ico\" type=\"image/vnd.microsoft.icon\">\n" +
            "    <link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\">\n" +
            "    <script src=\"js/main.js\" type=\"text/javascript\"></script>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>Hello World!</h1>\n" +
            "  </body>\n" +
            "</html>";

    /**
     * Empty Style
     */
    public static final String STYLE = "/* Add all your styles here */";

    /**
     * Empty Script
     */
    public static final String MAIN = "// Add all your JS here";

    /**
     * Log TAG
     */
    private static final String TAG = Project.class.getSimpleName();

    /**
     * Method to handle project creation
     *
     * @param context     used to show toasts
     * @param name        of project
     * @param author      of project
     * @param description of project
     * @param keywords    about project
     * @param stream      used for importing favicon
     */
    public static void generate(Context context, String name, String author, String description, String keywords, InputStream stream, ProjectAdapter adapter, View view) {
        String[] projects = new File(Constants.HYPER_ROOT).list();

        if (projects != null && Arrays.asList(projects).contains(name)) {
            Snackbar.make(view, name + " " + context.getString(R.string.already_exists) + ".", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (stream == null) {
            if (createDirectory(name)
                    && createDirectory(name + File.separator + "images")
                    && createDirectory(name + File.separator + "fonts")
                    && createDirectory(name + File.separator + "css")
                    && createDirectory(name + File.separator + "js")
                    && createFile(name, "index.html", INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords))
                    && createFile(name, "css" + File.separator + "style.css", STYLE)
                    && createFile(name, "js" + File.separator + "main.js", MAIN)
                    && Jason.createProjectFile(name, author, description, keywords)
                    && copyIcon(context, name)) {
                adapter.add(name);
                Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            if (createDirectory(name)
                    && createDirectory(name + File.separator + "images")
                    && createDirectory(name + File.separator + "fonts")
                    && createDirectory(name + File.separator + "css")
                    && createDirectory(name + File.separator + "js")
                    && createFile(name, "index.html", INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords))
                    && createFile(name, "css" + File.separator + "style.css", STYLE)
                    && createFile(name, "js" + File.separator + "main.js", MAIN)
                    && Jason.createProjectFile(name, author, description, keywords)
                    && copyIcon(name, stream)) {
                adapter.add(name);
                Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Check if project is valid
     *
     * @param string project title
     * @return true if project is valid
     */
    public static boolean isValid(String string) {
        return new File(Constants.HYPER_ROOT + File.separator + string + File.separator + ".hyperProps").exists()
                && new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "index.html").exists()
                && Jason.getProjectProperty(string, "name") != null
                && Jason.getProjectProperty(string, "author") != null
                && Jason.getProjectProperty(string, "description") != null
                && Jason.getProjectProperty(string, "keywords") != null;
    }

    /**
     * Method used to delete a project
     *
     * @param name of project
     * @return true if successfully deleted
     */
    public static boolean deleteProject(Context context, String name) {
        File projectDir = new File(Constants.HYPER_ROOT + File.separator + name);
        File[] files = projectDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(context, file);
            } else {
                if (!file.delete()) {
                    Log.e(TAG, context.getString(R.string.cannot_delete) + " " + file.getPath());
                }
            }
        }

        return projectDir.delete();
    }

    /**
     * Method used to delete directory
     *
     * @param directory to delete
     * @return true if successfully deleted
     */
    public static boolean deleteDirectory(Context context, File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(context, file);
                    } else {
                        if (!file.delete()) {
                            Log.e(TAG, context.getString(R.string.cannot_delete) + " " + file.getPath());
                        }
                    }
                }
            }
        }
        return (directory.delete());
    }

    /**
     * Method to get Favicon as Bitmap
     *
     * @param name of project
     * @return bitmap object of favicon
     */
    public static Bitmap getFavicon(String name) {
        return BitmapFactory.decodeFile(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico");
    }

    /**
     * Method used to create directory
     *
     * @param name of project
     * @return true if successfully create
     */
    static boolean createDirectory(String name) {
        return new File(Constants.HYPER_ROOT + File.separator + name).mkdirs();
    }

    /**
     * Method used for creation of files
     *
     * @param parent name of project
     * @param name of file
     * @param contents of file
     * @return true if successfully created
     */
    public static boolean createFile(String parent, String name, String contents) {
        try {
            OutputStream stream = new FileOutputStream(new File(Constants.HYPER_ROOT + File.separator + parent + File.separator + name));
            stream.write(contents.getBytes());
            stream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Method used to copy the default icon
     *
     * @param context used to open assets
     * @param name of projects
     * @return true if successfully copied
     */
    static boolean copyIcon(Context context, String name) {
        try {
            AssetManager manager = context.getAssets();
            InputStream stream = manager.open("web/favicon.ico");
            OutputStream output = new FileOutputStream(new File(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico"));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            stream.close();
            output.flush();
            output.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Method used to copy a custom icon
     *
     * @param name of project
     * @param stream containing path to custom icon
     * @return true if successfully copied
     */
    private static boolean copyIcon(String name, InputStream stream) {
        try {
            OutputStream outputStream = new FileOutputStream(new File(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico"));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            stream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     *  Guess whether given file is binary
     *  Just checks for anything under 0x09
     */
    public static boolean isBinaryFile(File f) {
        int result = 0;
        try {
            FileInputStream in = new FileInputStream(f);
            int size = in.available();
            if (size > 1024) size = 1024;
            byte[] data = new byte[size];
            result = in.read(data);
            in.close();

            int ascii = 0;
            int other = 0;

            for (byte b : data) {
                if (b < 0x09) return true;

                if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) ascii++;
                else if (b >= 0x20 && b <= 0x7E) ascii++;
                else other++;
            }

            return other != 0 && 100 * other / (ascii + other) > 95;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + String.valueOf(result));
        }

        return true;
    }

    /**
     * Check if file is an image
     *
     * @param f file to check
     * @return true if file is an image
     */
    public static boolean isImageFile(File f) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        return options.outWidth != -1 && options.outHeight != -1;
    }


    /**
     * Method used for importing images into project
     *
     * @param context used to getContentResolver
     * @param name of project
     * @param imageUri of chosen image
     * @param imageName of chosen image
     * @return true if successfully imported
     */
    public static boolean importImage(Context context, String name, Uri imageUri, String imageName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + imageName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream != null ? inputStream.read(buffer) : -1) != -1) {
                outputStream.write(buffer, 0, read);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Method used for importing fonts into project
     *
     * @param context used to getContentResolver
     * @param name of project
     * @param fontUri of chosen font
     * @param fontName of chosen font
     * @return true if successfully imported
     */
    public static boolean importFont(Context context, String name, Uri fontUri, String fontName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fontUri);
            OutputStream outputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + "fonts" + File.separator + fontName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream != null ? inputStream.read(buffer) : -1) != -1) {
                outputStream.write(buffer, 0, read);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Method used for importing CSS files into project
     *
     * @param context used to getContentResolver
     * @param name of project
     * @param cssUri of chosen CSS file
     * @param cssName of chosen CSS file
     * @return true if successfully imported
     */
    public static boolean importCss(Context context, String name, Uri cssUri, String cssName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(cssUri);
            OutputStream outputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + "css" + File.separator + cssName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream != null ? inputStream.read(buffer) : -1) != -1) {
                outputStream.write(buffer, 0, read);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Method used for importing JS files into project
     *
     * @param context used to getContentResolver
     * @param name    of project
     * @param jsUri   of chosen JS file
     * @param jsName  of chosen JS file
     * @return true if successfully imported
     */
    public static boolean importJs(Context context, String name, Uri jsUri, String jsName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(jsUri);
            OutputStream outputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + "js" + File.separator + jsName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream != null ? inputStream.read(buffer) : -1) != -1) {
                outputStream.write(buffer, 0, read);
            }

            if (inputStream != null) {
                inputStream.close();
            }

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }
}
