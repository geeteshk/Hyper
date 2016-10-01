package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(getPageTitle(position));
        return textView;
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

        imageView.setImageResource(Decor.getIcon(getPageTitle(position).toString(), mProject));
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
