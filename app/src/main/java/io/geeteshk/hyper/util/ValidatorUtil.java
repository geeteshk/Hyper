package io.geeteshk.hyper.util;

import android.support.design.widget.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import io.geeteshk.hyper.Constants;

/**
 * Utility class to validate project creation
 */
public class ValidatorUtil {

    /**
     * Method to validate project creation
     *
     * @param name        of project
     * @param author      of project
     * @param description of project
     * @param keywords    about project
     * @return true if valid
     */
    public static boolean validate(TextInputLayout name, TextInputLayout author, TextInputLayout description, TextInputLayout keywords) {
        if (name != null && name.getEditText().getText().toString().isEmpty()) {
            name.setError("Please enter a name.");
            return false;
        }

        if (author.getEditText().getText().toString().isEmpty()) {
            author.setError("Please enter an author.");
            return false;
        }

        if (description.getEditText().getText().toString().isEmpty()) {
            description.setError("Please enter a description.");
            return false;
        }

        if (keywords.getEditText().getText().toString().isEmpty()) {
            keywords.setError("Please enter some keywords.");
            return false;
        }

        return true;
    }

    public static void removeBroken(ArrayList objectsList) {
        for (Iterator<String> iterator = objectsList.iterator(); iterator.hasNext(); ) {
            String string = iterator.next();
            if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + string + ".hyper").exists() ||
                    !new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "index.html").exists() ||
                    !new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "js" + File.separator + "main.js").exists() ||
                    !new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "css" + File.separator + "style.css").exists() ||
                    !new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "images" + File.separator + "favicon.ico").exists() ||
                    !new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "fonts").isDirectory()) {
                iterator.remove();
            }
        }
    }
}
