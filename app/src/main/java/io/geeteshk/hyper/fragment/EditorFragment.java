package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.geeteshk.hyper.Constants;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.PreferenceUtil;
import io.geeteshk.hyper.util.ProjectUtil;
import io.geeteshk.hyper.widget.Editor;

/**
 * Fragment used to edit files
 */
public class EditorFragment extends Fragment {

    /**
     * Strings representing project and filename
     */
    String mProject, mFilename;

    /**
     * Default empty constructor
     */
    public EditorFragment() {
        setRetainInstance(true);
    }

    /**
     * Method used to inflate and setup view
     *
     * @param inflater           used to inflate layout
     * @param container          parent view
     * @param savedInstanceState restores state onResume
     * @return fragment view that is created
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editor, container, false);

        boolean darkTheme = PreferenceUtil.get(getActivity(), "dark_theme", false);
        final Editor editText = (Editor) rootView.findViewById(R.id.file_content);
        LinearLayout symbolLayout = (LinearLayout) rootView.findViewById(R.id.symbol_layout);
        ImageButton symbolTab = (ImageButton) rootView.findViewById(R.id.symbol_tab);
        Button symbolOne = (Button) rootView.findViewById(R.id.symbol_one);
        Button symbolTwo = (Button) rootView.findViewById(R.id.symbol_two);
        Button symbolThree = (Button) rootView.findViewById(R.id.symbol_three);
        Button symbolFour = (Button) rootView.findViewById(R.id.symbol_four);
        Button symbolFive = (Button) rootView.findViewById(R.id.symbol_five);
        Button symbolSix = (Button) rootView.findViewById(R.id.symbol_six);
        Button symbolSeven = (Button) rootView.findViewById(R.id.symbol_seven);
        Button symbolEight = (Button) rootView.findViewById(R.id.symbol_eight);

        if (mFilename.endsWith(".html")) {
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
        } else if (mFilename.endsWith(".css")) {
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
        } else if (mFilename.endsWith(".js")) {
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

        if (!darkTheme) {
            symbolLayout.setBackgroundColor(0xFF333333);
            symbolTab.setImageResource(R.drawable.ic_tab);
            symbolOne.setTextColor(0xFFFFFFFF);
            symbolTwo.setTextColor(0xFFFFFFFF);
            symbolThree.setTextColor(0xFFFFFFFF);
            symbolFour.setTextColor(0xFFFFFFFF);
            symbolFive.setTextColor(0xFFFFFFFF);
            symbolSix.setTextColor(0xFFFFFFFF);
            symbolSeven.setTextColor(0xFFFFFFFF);
            symbolEight.setTextColor(0xFFFFFFFF);
        }

        String contents = getContents(mProject, mFilename);
        editText.setTextHighlighted(contents);
        editText.mOnTextChangedListener = new Editor.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                ProjectUtil.createFile(mProject, mFilename, editText.getText().toString());
            }
        };

        return rootView;
    }

    private void setSymbol(Editor editor, Button button, String symbol) {
        button.setText(symbol);
        button.setOnClickListener(new SymbolClickListener(editor, symbol));
    }

    private void setSymbol(Editor editor, ImageButton button, String symbol) {
        button.setOnClickListener(new SymbolClickListener(editor, symbol));
    }

    /**
     * Method used to get contents of files
     *
     * @param project  name of project
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
            e.printStackTrace();
        }

        return "Unable to read file!";
    }

    /**
     * Sets the project name
     *
     * @param project name of project
     */
    public void setProject(String project) {
        this.mProject = project;
    }

    /**
     * Sets the filename
     *
     * @param filename name of file
     */
    public void setFilename(String filename) {
        this.mFilename = filename;
    }

    private class SymbolClickListener implements View.OnClickListener {

        private Editor mEditor;
        private String mSymbol;

        public SymbolClickListener(Editor editor, String symbol) {
            mEditor = editor;
            mSymbol = symbol;
        }

        @Override
        public void onClick(View v) {
            int start = Math.max(mEditor.getSelectionStart(), 0);
            int end = Math.max(mEditor.getSelectionEnd(), 0);
            mEditor.getText().replace(Math.min(start, end), Math.max(start, end),
                    mSymbol, 0, mSymbol.length());
        }
    }
}
