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

package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.widget.Editor;

/**
 * Fragment used to edit files
 */
public class EditorFragment extends Fragment {

    /**
     * Log TAG
     */
    private static final String TAG = EditorFragment.class.getSimpleName();

    /**
     * public Constructor
     */
    public EditorFragment() {
        setRetainInstance(true);
    }

    /**
     * Called when fragment view is created
     *
     * @param inflater used to inflate layout resource
     * @param container parent view
     * @param savedInstanceState state to be restored
     * @return inflated view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String location = getArguments().getString("location");
        final String project = location.substring(0, location.indexOf(File.separator));
        final String filename = location.substring(location.indexOf(File.separator), location.length());
        if (!new File(Constants.HYPER_ROOT + File.separator + project, filename).exists()) {
            TextView textView = new TextView(getActivity());
            textView.setText(R.string.file_problem);
            return textView;
        }

        View rootView = inflater.inflate(R.layout.fragment_editor, container, false);

        final Editor editText = (Editor) rootView.findViewById(R.id.file_content);
        FloatingActionButton symbolTab = (FloatingActionButton) rootView.findViewById(R.id.symbol_tab);
        Button symbolOne = (Button) rootView.findViewById(R.id.symbol_one);
        Button symbolTwo = (Button) rootView.findViewById(R.id.symbol_two);
        Button symbolThree = (Button) rootView.findViewById(R.id.symbol_three);
        Button symbolFour = (Button) rootView.findViewById(R.id.symbol_four);
        Button symbolFive = (Button) rootView.findViewById(R.id.symbol_five);
        Button symbolSix = (Button) rootView.findViewById(R.id.symbol_six);
        Button symbolSeven = (Button) rootView.findViewById(R.id.symbol_seven);
        Button symbolEight = (Button) rootView.findViewById(R.id.symbol_eight);

        if (filename.endsWith(".html") || filename.equals("imports.txt")) {
            editText.setType(Editor.CodeType.HTML);
            setSymbol(editText, symbolTab, "\t\t");
            setSymbol(editText, symbolOne, "<");
            setSymbol(editText, symbolTwo, "/");
            setSymbol(editText, symbolThree, ">");
            setSymbol(editText, symbolFour, "\"");
            setSymbol(editText, symbolFive, "=");
            setSymbol(editText, symbolSix, "!");
            setSymbol(editText, symbolSeven, "-");
            setSymbol(editText, symbolEight, "/");
        } else if (filename.endsWith(".css")) {
            editText.setType(Editor.CodeType.CSS);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, ":");
            setSymbol(editText, symbolFour, ",");
            setSymbol(editText, symbolFive, "#");
            setSymbol(editText, symbolSix, ".");
            setSymbol(editText, symbolSeven, ";");
            setSymbol(editText, symbolEight, "-");
        } else if (filename.endsWith(".js")) {
            editText.setType(Editor.CodeType.JS);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, "(");
            setSymbol(editText, symbolFour, ")");
            setSymbol(editText, symbolFive, "!");
            setSymbol(editText, symbolSix, "=");
            setSymbol(editText, symbolSeven, ":");
            setSymbol(editText, symbolEight, "?");
        }

        String contents = getContents(project, filename);
        editText.setTextHighlighted(contents);
        editText.mOnTextChangedListener = new Editor.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                Project.createFile(project, filename, editText.getText().toString());
            }
        };

        return rootView;
    }

    /**
     * Method to set symbol
     *
     * @param editor editor to set for
     * @param button which button to set to
     * @param symbol which symbol to set
     */
    private void setSymbol(Editor editor, Button button, String symbol) {
        button.setText(symbol);
        button.setOnClickListener(new SymbolClickListener(editor, symbol));
    }

    /**
     * Method to set symbol
     *
     * @param editor editor to set for
     * @param button which image button to set to
     * @param symbol which symbol to set
     */
    private void setSymbol(Editor editor, ImageButton button, String symbol) {
        button.setOnClickListener(new SymbolClickListener(editor, symbol));
    }

    /**
     * Method used to get contents of files
     *
     * @param project name of project
     * @param filename name of file
     * @return contents of file
     */
    private String getContents(String project, String filename) {
        try {
            InputStream inputStream = new FileInputStream(Constants.HYPER_ROOT + File.separator + project + File.separator + filename);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append('\n');
            }

            return builder.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return "Unable to read file!";
    }

    /**
     * Listener to add symbol to text
     */
    private class SymbolClickListener implements View.OnClickListener {

        /**
         * Editor to add symbol to
         */
        private Editor mEditor;

        /**
         * Symbol to add to editor
         */
        private String mSymbol;

        /**
         * Constructor
         *
         * @param editor see mEditor
         * @param symbol see mSymbol
         */
        SymbolClickListener(Editor editor, String symbol) {
            mEditor = editor;
            mSymbol = symbol;
        }

        /**
         * Called when view is clicked
         *
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v) {
            int start = Math.max(mEditor.getSelectionStart(), 0);
            int end = Math.max(mEditor.getSelectionEnd(), 0);
            mEditor.getText().replace(Math.min(start, end), Math.max(start, end),
                    mSymbol, 0, mSymbol.length());
        }
    }
}
