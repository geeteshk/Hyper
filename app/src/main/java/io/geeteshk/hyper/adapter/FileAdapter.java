package io.geeteshk.hyper.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.geeteshk.hyper.fragment.EditorFragment;

/**
 * Adapter to load main files into editor
 */
public class FileAdapter extends FragmentPagerAdapter {

    /**
     * Names of main files to edit
     */
    String[] mFiles = {"index.html", "style.css", "main.js"};

    /**
     * Name of currently opened project
     */
    String mProject;

    /**
     * Public constructor for adapter
     *
     * @param fm fragmentManager used to add fragment
     */
    public FileAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Method to specify which project to load files from
     *
     * @param project name for parent directory / project
     */
    public void setProject(String project) {
        this.mProject = project;
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
        editorFragment.setFilename(mFiles[position]);
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
        return mFiles[position];
    }

    /**
     * Method to return number of fragments
     *
     * @return always three
     */
    @Override
    public int getCount() {
        return mFiles.length;
    }
}
