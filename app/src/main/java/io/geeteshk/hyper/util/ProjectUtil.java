package io.geeteshk.hyper.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Utility class to handle all project related tasks
 */
public class ProjectUtil {

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
            "    <link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\">\n" +
            "    <script src=\"main.js\" type=\"text/javascript\"></script>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>Hello World!</h1>\n" +
            "  </body>\n" +
            "</html>";

    /**
     * Empty Style
     */
    public static final String STYLE = "";

    /**
     * Empty Script
     */
    public static final String MAIN = "";

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
    public static void generate(Context context, String name, String author, String description, String keywords, InputStream stream) {
        if (Arrays.asList(new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper").list()).contains(name)) {
            Toast.makeText(context, name + " already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (stream == null) {
            if (createDirectory(name)
                    && createDirectory(name + File.separator + "images")
                    && createDirectory(name + File.separator + "fonts")
                    && createDirectory(name + File.separator + "css")
                    && createDirectory(name + File.separator + "js")
                    && createFile(name, "index.html", INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords))
                    && createFile(name, "style.css", STYLE)
                    && createFile(name, "main.js", MAIN)
                    && JsonUtil.createProjectFile(name, author, description, keywords)
                    && copyIcon(context, name)) {
                Toast.makeText(context, "Your project has been successfully created!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "It seems something went wrong while creating the project.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (createDirectory(name)
                    && createDirectory(name + File.separator + "images")
                    && createDirectory(name + File.separator + "fonts")
                    && createDirectory(name + File.separator + "css")
                    && createDirectory(name + File.separator + "js")
                    && createFile(name, "index.html", INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords))
                    && createFile(name, "style.css", STYLE)
                    && createFile(name, "main.js", MAIN)
                    && JsonUtil.createProjectFile(name, author, description, keywords)
                    && copyIcon(name, stream)) {
                Toast.makeText(context, "Your project has been successfully created!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "It seems something went wrong while creating the project.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method used to delete a project
     *
     * @param name of project
     * @return true if successfully deleted
     */
    public static boolean deleteProject(String name) {
        File projectDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name);
        File[] files = projectDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
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
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
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
        return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "images" + File.separator + "favicon.ico");
    }

    /**
     * Method used to create directory
     *
     * @param name of project
     * @return true if successfully create
     */
    private static boolean createDirectory(String name) {
        return new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name).mkdirs();
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
            OutputStream stream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + parent + File.separator + name));
            stream.write(contents.getBytes());
            stream.close();
        } catch (Exception e) {
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
    private static boolean copyIcon(Context context, String name) {
        try {
            AssetManager manager = context.getAssets();
            InputStream stream = manager.open("web/favicon.ico");
            OutputStream output = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "images" + File.separator + "favicon.ico"));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            stream.close();
            output.flush();
            output.close();
        } catch (Exception e) {
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
            OutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "images" + File.separator + "favicon.ico"));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            stream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            return false;
        }

        return true;
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
            OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "images" + File.separator + imageName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
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
            OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "fonts" + File.separator + fontName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
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
            OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "css" + File.separator + cssName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
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
            OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + name + File.separator + "js" + File.separator + jsName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
