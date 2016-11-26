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
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
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
    static String createProjectFile(String name, String author, String description, String keywords) {
        JSONObject object = new JSONObject();
        try {
            object.put("name", name);
            object.put("author", author);
            object.put("description", description);
            object.put("keywords", keywords);
            return object.toString(4);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    /**
     * Reads contents from project file
     *
     * @param name    of project
     * @return project file contents
     */
    private static String getProjectJSON(String name) {
        try {
            InputStream inputStream = new FileInputStream(Constants.HYPER_ROOT + File.separator + name + File.separator + ".hyperProps");
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