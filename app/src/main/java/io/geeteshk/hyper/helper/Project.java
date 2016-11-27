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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
    private static final String INDEX = "<!doctype html>\n" +
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
    private static final String STYLE = "/* Add all your styles here */";

    /**
     * Empty Script
     */
    private static final String MAIN = "// Add all your JS here";

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

        File projectFile = new File(Constants.HYPER_ROOT + File.separator + name);
        File cssFile = new File(projectFile, "css");
        File jsFile = new File(projectFile, "js");
        try {
            // Create project tree
            FileUtils.forceMkdir(projectFile);
            FileUtils.forceMkdir(new File(projectFile, "images"));
            FileUtils.forceMkdir(new File(projectFile, "fonts"));
            FileUtils.forceMkdir(cssFile);
            FileUtils.forceMkdir(jsFile);

            // Create necessary files
            FileUtils.writeStringToFile(new File(projectFile, "index.html"), getIndex(name, author, description, keywords), Charset.defaultCharset());
            FileUtils.writeStringToFile(new File(cssFile, "style.css"), STYLE, Charset.defaultCharset());
            FileUtils.writeStringToFile(new File(jsFile, "main.js"), MAIN, Charset.defaultCharset());
            FileUtils.writeStringToFile(new File(projectFile, ".hyperProps"), Jason.createProjectFile(name, author, description, keywords), Charset.defaultCharset());

            // Copy icon
            if (stream == null) {
                copyIcon(context, name);
            } else {
                copyIcon(name, stream);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show();
            return;
        }

        adapter.add(name);
        Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show();
    }

    private static String getIndex(String name, String author, String description, String keywords) {
        return INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords);
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
    public static boolean deleteProject(String name) {
        File projectDir = new File(Constants.HYPER_ROOT + File.separator + name);
        try {
            FileUtils.deleteDirectory(projectDir);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }

    /**
     * Method to get Favicon as Bitmap
     *
     * @param name of project
     * @return bitmap object of favicon
     */
    public static Bitmap getFavicon(String name) {
        if (new File(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico").exists()) {
            return BitmapFactory.decodeFile(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico");
        } else {
            return Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        }
    }

    /**
     * Method used to copy the default icon
     *
     * @param context used to open assets
     * @param name of projects
     * @return true if successfully copied
     */
    private static boolean copyIcon(Context context, String name) {
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

    public static boolean importFile(Context context, String name, Uri fileUri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            OutputStream outputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + fileName);
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
