package io.geeteshk.hyper.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import io.geeteshk.hyper.Constants;
import io.geeteshk.hyper.R;

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
    public static boolean validate(Context context, @Nullable TextInputLayout name, TextInputLayout author, TextInputLayout description, TextInputLayout keywords) {
        if (name != null) {
            assert name.getEditText() != null;
            if (name.getEditText().getText().toString().isEmpty()) {
                name.setError(context.getString(R.string.name_error));
                return false;
            }
        }

        assert author.getEditText() != null;
        if (author.getEditText().getText().toString().isEmpty()) {
            author.setError(context.getString(R.string.author_error));
            return false;
        }

        assert description.getEditText() != null;
        if (description.getEditText().getText().toString().isEmpty()) {
            description.setError(context.getString(R.string.desc_error));
            return false;
        }

        assert keywords.getEditText() != null;
        if (keywords.getEditText().getText().toString().isEmpty()) {
            keywords.setError(context.getString(R.string.keywords_error));
            return false;
        }

        return true;
    }

    public static void removeBroken(ArrayList objectsList) {
        for (Iterator iterator = objectsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
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
