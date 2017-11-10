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
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.ResourceHelper;

/**
 * Adapter to load main files into editor
 */
public class FileAdapter extends ArrayAdapter<String> {

    /**
     * Names of main files to edit
     */
    private List<String> openFiles;

    /**
     * Public constructor for adapter
     */
    public FileAdapter(Context context, List<String> files) {
        super(context, android.R.layout.simple_list_item_1, files);
        openFiles = files;
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

        ImageView imageView = rootView.findViewById(R.id.file_icon);
        TextView textView = rootView.findViewById(R.id.file_title);

        int resource = ResourceHelper.getIcon(new File(openFiles.get(position)));
        imageView.setImageResource(resource);
        textView.setText(getPageTitle(position));
        textView.setTypeface(Typeface.DEFAULT_BOLD);

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

        ImageView imageView = rootView.findViewById(R.id.file_icon);
        TextView textView = rootView.findViewById(R.id.file_title);

        imageView.setImageResource(ResourceHelper.getIcon(new File(openFiles.get(position))));
        textView.setText(getPageTitle(position));

        return rootView;
    }

    /**
     * Method to remove folder name from file
     *
     * @param position item position
     * @return new page title
     */
    private CharSequence getPageTitle(int position) {
        return new File(openFiles.get(position)).getName();
    }

    /**
     * Gets count of files open
     *
     * @return array size
     */
    @Override
    public int getCount() {
        return openFiles.size();
    }
}
