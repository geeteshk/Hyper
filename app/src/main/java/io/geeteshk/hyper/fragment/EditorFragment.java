package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.geeteshk.hyper.R;
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

        final Editor editText = (Editor) rootView.findViewById(R.id.file_content);

        if (mFilename.endsWith(".html")) {
            editText.setType(Editor.CodeType.HTML);
        } else if (mFilename.endsWith(".css")) {
            editText.setType(Editor.CodeType.CSS);
        } else if (mFilename.endsWith(".js")) {
            editText.setType(Editor.CodeType.JS);
        }

        editText.setTextHighlighted(getContents(mProject, mFilename));
        editText.mOnTextChangedListener = new Editor.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                ProjectUtil.createFile(mProject, mFilename, editText.getText().toString());
            }
        };

        return rootView;
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
            InputStream inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "Hyper" + File.separator + project + File.separator + filename);
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

        return null;
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
}
