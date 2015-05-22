package io.geeteshk.hyper.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Utility class to validate project creation
 */
public class ValidatorUtil {

    /**
     * Method to validate project creation
     *
     * @param context     used to show toasts
     * @param name        of project
     * @param author      of project
     * @param description of project
     * @param keywords    about project
     * @return true if valid
     */
    public static boolean validate(Context context, String name, String author, String description, String keywords) {
        if (name.isEmpty()) {
            Toast.makeText(context, "Oops! Looks like you forgot to enter a name for your project.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (author.isEmpty()) {
            Toast.makeText(context, "Oops! Looks like you forgot to enter your name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(context, "Oops! Looks like you forgot to enter a description.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (keywords.isEmpty()) {
            Toast.makeText(context, "Oops! Looks like you forgot to enter some keywords.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
