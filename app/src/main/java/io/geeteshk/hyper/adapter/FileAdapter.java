package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import io.geeteshk.hyper.fragment.EditorFragment;
import io.geeteshk.hyper.fragment.ImageFragment;

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

    List<Fragment> mFragments;
    Context mContext;

    /**
     * Public constructor for adapter
     *
     * @param fm fragmentManager used to add fragment
     */
    public FileAdapter(Context context, FragmentManager fm, String project, List<String> files, List<Fragment> fragments) {
        super(fm);
        mFiles = files;
        mProject = project;
        mFragments = fragments;
        mContext = context;
    }

    /**
     * Creates a new EditorFragment and sets the file contents to it
     *
     * @param position current fragment position
     * @return specified fragment
     */
    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
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
        } else if (mFiles.get(position).startsWith("images")) {
            return mFiles.get(position).substring(7);
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
        return mFragments.size();
    }

    public void add(String title, Bundle b, boolean image) {
        if (image) {
            ImageFragment imageFragment = (ImageFragment) Fragment.instantiate(mContext, ImageFragment.class.getName(), b);
            imageFragment.setProject(mProject);
            imageFragment.setFilename(title);
            mFragments.add(imageFragment);
            mFiles.add(title);
        } else {
            EditorFragment editorFragment = (EditorFragment) Fragment.instantiate(mContext, EditorFragment.class.getName(), b);
            editorFragment.setProject(mProject);
            editorFragment.setFilename(title);
            mFragments.add(editorFragment);
            mFiles.add(title);
        }
    }
}
