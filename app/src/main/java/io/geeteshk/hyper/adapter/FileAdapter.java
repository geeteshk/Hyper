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

package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Decor;

/**
 * Adapter to load main files into editor
 */
public class FileAdapter extends ArrayAdapter<String> {

    /**
     * Names of main files to edit
     */
    private List<String> mFiles;

    /**
     * Name of currently opened project
     */
    private String mProject;

    /**
     * Public constructor for adapter
     */
    public FileAdapter(Context context, String project, List<String> files) {
        super(context, android.R.layout.simple_list_item_1, files);
        mFiles = files;
        mProject = project;
    }

    /**
     * View is created
     *
     * @param position item position
     * @param convertView reuseable view
     * @param parent parent view
     * @return view to display
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rootView;
        if (convertView == null) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_project, parent, false);
        } else {
            rootView = convertView;
        }

        ImageView imageView = (ImageView) rootView.findViewById(R.id.file_icon);
        TextView textView = (TextView) rootView.findViewById(R.id.file_title);

        int resource = Decor.getIcon(mFiles.get(position), mProject);
        imageView.setImageResource(resource);
        switch (resource) {
            case R.drawable.ic_font:case R.drawable.ic_file:case R.drawable.ic_folder:case R.drawable.ic_image:
                imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.whiteButNotAndroidWhite), PorterDuff.Mode.SRC_ATOP);
                break;
        }

        textView.setText(getPageTitle(position));
        textView.setTextColor(0xffffffff);
        textView.setTypeface(Typeface.SERIF);

        return rootView;
    }

    /**
     * Dropdown view is created
     *
     * @param position item position
     * @param convertView reuseable view
     * @param parent parent view
     * @return view to display in dropdown
     */
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View rootView;
        if (convertView == null) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_project, parent, false);
        } else {
            rootView = convertView;
        }

        ImageView imageView = (ImageView) rootView.findViewById(R.id.file_icon);
        TextView textView = (TextView) rootView.findViewById(R.id.file_title);

        imageView.setImageResource(Decor.getIcon(mFiles.get(position), mProject));
        textView.setText(getPageTitle(position));
        textView.setTypeface(Typeface.SERIF);

        return rootView;
    }

    /**
     * Method to remove folder name from file
     *
     * @param position item position
     * @return new page title
     */
    private CharSequence getPageTitle(int position) {
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
     * Gets count of files open
     *
     * @return array size
     */
    @Override
    public int getCount() {
        return mFiles.size();
    }
}
