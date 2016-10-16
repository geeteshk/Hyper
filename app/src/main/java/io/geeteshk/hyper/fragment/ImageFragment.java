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
     * public Constructor
     */
    public ImageFragment() {}

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
        File file = new File(Constants.HYPER_ROOT + File.separator + getArguments().getString("location"));
        if (!file.exists()) {
            TextView textView = new TextView(getActivity());
            textView.setText(R.string.file_problem);
            return textView;
        }

        final BitmapDrawable drawable = new BitmapDrawable(getActivity().getResources(), file.getAbsolutePath());
        final ImageView imageView = new ImageView(getActivity());
        final String fileSize = getSize(file);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageDrawable(drawable);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Snackbar snackbar = Snackbar.make(imageView, drawable.getIntrinsicWidth() + "x" + drawable.getIntrinsicHeight() + "px " + fileSize, Snackbar.LENGTH_INDEFINITE);
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
