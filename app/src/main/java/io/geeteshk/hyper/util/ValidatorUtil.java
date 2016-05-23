package io.geeteshk.hyper.util;

import android.support.design.widget.TextInputLayout;

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
        if (name.getEditText().getText().toString().isEmpty()) {
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
}
