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
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;

import java.util.ArrayList;
import java.util.Iterator;

import io.geeteshk.hyper.R;

/**
 * Helper class to validate project creation
 */
public class DataValidator {

    /**
     * Method to validate project creation
     *
     * @param name        of project
     * @param author      of project
     * @param description of project
     * @param keywords    about project
     * @return true if valid
     */
    public static boolean validateCreate(Context context, @Nullable TextInputLayout name, TextInputLayout author, TextInputLayout description, TextInputLayout keywords) {
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

    public static boolean validateClone(Context context, TextInputEditText name, TextInputEditText remote) {
        if (name.getText().toString().isEmpty()) {
            name.setError(context.getString(R.string.name_error));
            return false;
        }

        if (remote.getText().toString().isEmpty()) {
            remote.setError(context.getString(R.string.remote_error));
            return false;
        }

        return true;
    }

    /**
     * Removes broken projects from list
     *
     * @param objectsList to remove projects from
     */
    public static void removeBroken(ArrayList objectsList) {
        for (Iterator iterator = objectsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!ProjectManager.isValid(string)) {
                iterator.remove();
            }
        }
    }
}
