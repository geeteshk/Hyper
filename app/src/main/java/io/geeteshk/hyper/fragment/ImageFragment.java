package io.geeteshk.hyper.fragment;


import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Constants;

/**
 * Fragment to view image
 */
public class ImageFragment extends Fragment {

    /**
     * Location of image within project
     */
    private String mLocation;

    /**
     * Project containing image
     */
    private String mProject;

    /**
     * public Constructor
     */
    public ImageFragment() {}

    /**
     * Setter for location
     *
     * @param location see mLocation
     */
    public void setFilename(String location) {
        mLocation = location;
    }

    /**
     * Setter for project
     *
     * @param project see mProject
     */
    public void setProject(String project) {
        mProject = project;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final File file = new File(Constants.HYPER_ROOT + File.separator + mProject, mLocation);
        if (!file.exists()) {
            TextView textView = new TextView(getActivity());
            textView.setText(R.string.file_problem);
            return textView;
        }

        final BitmapDrawable drawable = new BitmapDrawable(getActivity().getResources(), file.getAbsolutePath());
        final ImageView imageView = new ImageView(getActivity());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageDrawable(drawable);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Snackbar snackbar = Snackbar.make(imageView, drawable.getIntrinsicWidth() + "x" + drawable.getIntrinsicHeight() + "px " + getSize(file), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });

                snackbar.show();
            }
        });

        return imageView;
    }

    /**
     * Gets file size
     *
     * @param f file to get size
     * @return string containing file size and measurement
     */
    private String getSize(File f) {
        long size = f.length() / 1024;
        if (size >= 1024) {
            return size / 1024 + " MB";
        } else {
            return size + " KB";
        }
    }
}
