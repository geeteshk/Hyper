package io.geeteshk.hyper.helper;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Utility class to handle JSON
 */
public class Jason {

    private static final String TAG = Jason.class.getSimpleName();

    /**
     * Creates the .hyper file at the root of a project
     *
     * @param name        of the project
     * @param author      of the project
     * @param description of the project
     * @param keywords    about the project
     * @return true if successful
     */
    static boolean createProjectFile(String name, String author, String description, String keywords, String color) {
        try {
            JSONObject object = new JSONObject();
            object.put("name", name);
            object.put("author", author);
            object.put("description", description);
            object.put("keywords", keywords);
            object.put("color", color);

            OutputStream stream = new FileOutputStream(new File(Constants.HYPER_ROOT + File.separator + name + File.separator + name + ".hyper"));
            stream.write(object.toString(4).getBytes());
            stream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Reads contents from project file
     *
     * @param name    of project
     * @return project file contents
     */
    private static String getProjectJSON(String name) {
        try {
            InputStream inputStream = new FileInputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + name + ".hyper");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append(System.getProperty("line.separator"));
            }

            return builder.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return "";
    }

    /**
     * Reads properties from project JSON
     *
     * @param name    of project
     * @param prop    property that needs to be read
     * @return value of property
     */
    public static String getProjectProperty(String name, String prop) {
        try {
            String json = getProjectJSON(name);
            JSONObject object = new JSONObject(json);
            return object.getString(prop);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return "";
    }
}