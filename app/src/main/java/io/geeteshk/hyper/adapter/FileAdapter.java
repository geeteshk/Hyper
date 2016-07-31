package io.geeteshk.hyper.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import io.geeteshk.hyper.fragment.EditorFragment;

/**
 * Adapter to load main files into editor
 */
public class FileAdapter extends FragmentPagerAdapter {

    /**
     * Names of main files to edit
     */
    List<String> mFiles;

    /**
     * Name of currently opened project
     */
    String mProject;

    /**
     * Public constructor for adapter
     *
     * @param fm fragmentManager used to add fragment
     */
    public FileAdapter(FragmentManager fm, String project, List<String> files) {
        super(fm);
        mFiles = files;
        mProject = project;
    }

    /**
     * Creates a new EditorFragment and sets the file contents to it
     *
     * @param position current fragment position
     * @return specified fragment
     */
    @Override
    public Fragment getItem(int position) {
        EditorFragment editorFragment = new EditorFragment();
        editorFragment.setProject(mProject);
        editorFragment.setFilename(mFiles.get(position));
        return editorFragment;
    }

    /**
     * Required for tabs to work correctly
     *
     * @param position current fragment position
     * @return current page title
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (mFiles.get(position).startsWith("css")) {
            return mFiles.get(position).substring(4);
        } else if (mFiles.get(position).startsWith("js")) {
            return mFiles.get(position).substring(3);
        }

        return mFiles.get(position);
    }

    /**
     * Method to return number of fragments
     *
     * @return always three
     */
    @Override
    public int getCount() {
        return mFiles.size();
    }
}
