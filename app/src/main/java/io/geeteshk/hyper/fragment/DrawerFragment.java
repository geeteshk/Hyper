package io.geeteshk.hyper.fragment;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.PreferenceUtil;

/**
 * Fragment to show drawer content
 */
public class DrawerFragment extends Fragment {

    /**
     * Currently and previously clicked ImageViews
     */
    static ImageView mOldImage, mCurrentImage;
    /**
     * Currently and previously clicked TextViews
     */
    static TextView mOldView, mCurrentView;
    /**
     * Different layout options
     */
    private static LinearLayout[] mLayouts;

    /**
     * Default empty constructor
     */
    public DrawerFragment() {
    }

    /**
     * Highlights drawer item at position
     *
     * @param context  used to get preferences
     * @param position drawer item position
     */
    public static void select(Context context, int position) {
        View view = mLayouts[position];
        for (LinearLayout layout : mLayouts) {
            layout.setActivated(false);
        }

        view.setActivated(true);
        if (mOldView != null) {
            if (PreferenceUtil.get(context, "dark_theme", false)) {
                mOldView.setTextColor(context.getResources().getColor(R.color.primary_text_default_material_dark));
            } else {
                mOldView.setTextColor(context.getResources().getColor(R.color.primary_text_default_material_light));
            }

            mOldView.setAlpha(0.87f);
        }

        mCurrentView = (TextView) view.findViewById(R.id.text_view);
        mCurrentView.setTextColor(0xff2196f3);
        mCurrentView.setAlpha(1f);
        mOldView = mCurrentView;

        if (mOldImage != null) {
            if (PreferenceUtil.get(context, "dark_theme", false)) {
                mOldImage.getDrawable().setColorFilter(context.getResources().getColor(R.color.primary_text_default_material_dark), PorterDuff.Mode.SRC_ATOP);
            } else {
                mOldImage.getDrawable().clearColorFilter();
            }

            mOldImage.setAlpha(0.54f);
        }

        mCurrentImage = (ImageView) view.findViewById(R.id.image_view);
        mCurrentImage.setAlpha(1f);
        mCurrentImage.getDrawable().setColorFilter(0xff2196f3, PorterDuff.Mode.SRC_ATOP);
        mOldImage = mCurrentImage;
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
        View rootView = inflater.inflate(R.layout.fragment_drawer, container, false);

        LinearLayout create = (LinearLayout) rootView.findViewById(R.id.create);
        LinearLayout edit = (LinearLayout) rootView.findViewById(R.id.edit);
        LinearLayout pilot = (LinearLayout) rootView.findViewById(R.id.pilot);
        LinearLayout contribute = (LinearLayout) rootView.findViewById(R.id.contribute);
        LinearLayout donate = (LinearLayout) rootView.findViewById(R.id.donate);
        LinearLayout settings = (LinearLayout) rootView.findViewById(R.id.settings);
        LinearLayout help = (LinearLayout) rootView.findViewById(R.id.help);

        create.setOnClickListener(new DrawerTouchListener(0));
        edit.setOnClickListener(new DrawerTouchListener(1));
        pilot.setOnClickListener(new DrawerTouchListener(2));
        contribute.setOnClickListener(new DrawerTouchListener(3));
        donate.setOnClickListener(new DrawerTouchListener(4));
        settings.setOnClickListener(new DrawerTouchListener(5));
        help.setOnClickListener(new DrawerTouchListener(6));

        mLayouts = new LinearLayout[]{create, edit, pilot, contribute, donate, settings, help};
        if (PreferenceUtil.get(getActivity(), "dark_theme", false)) {
            for (LinearLayout linearLayout : mLayouts) {
                ((ImageView) linearLayout.findViewById(R.id.image_view)).getDrawable().setColorFilter(getActivity().getResources().getColor(R.color.primary_text_default_material_dark), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return rootView;
    }

    /**
     * Listener to handle drawer item clicks
     */
    private class DrawerTouchListener implements View.OnClickListener {

        /**
         * Position of view selected
         */
        int mPosition;

        /**
         * Constructor with custom int parameter
         *
         * @param position selected drawer item position
         */
        public DrawerTouchListener(int position) {
            this.mPosition = position;
        }

        /**
         * Called when particular view is clicked
         *
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            for (LinearLayout layout : mLayouts) {
                layout.setActivated(false);
            }

            v.setActivated(true);
            if (mOldView != null) {
                if (PreferenceUtil.get(getActivity(), "dark_theme", false)) {
                    mOldView.setTextColor(getActivity().getResources().getColor(R.color.primary_text_default_material_dark));
                } else {
                    mOldView.setTextColor(getActivity().getResources().getColor(R.color.primary_text_default_material_light));
                }

                mOldView.setAlpha(0.87f);
            }

            mCurrentView = (TextView) v.findViewById(R.id.text_view);
            mCurrentView.setTextColor(0xff2196f3);
            mCurrentView.setAlpha(1f);
            mOldView = mCurrentView;

            if (mOldImage != null) {
                if (PreferenceUtil.get(getActivity(), "dark_theme", false)) {
                    mOldImage.getDrawable().setColorFilter(getActivity().getResources().getColor(R.color.primary_text_default_material_dark), PorterDuff.Mode.SRC_ATOP);
                } else {
                    mOldImage.getDrawable().clearColorFilter();
                }

                mOldImage.setAlpha(0.54f);
            }

            mCurrentImage = (ImageView) v.findViewById(R.id.image_view);
            mCurrentImage.setAlpha(1f);
            mCurrentImage.getDrawable().setColorFilter(0xff2196f3, PorterDuff.Mode.SRC_ATOP);
            mOldImage = mCurrentImage;

            MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), mPosition);
        }
    }
}
