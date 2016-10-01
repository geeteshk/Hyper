package io.geeteshk.hyper.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import java.io.File;

import io.geeteshk.hyper.R;

/**
 * Helper class used for decor related functions
 */
public class Decor {

    /**
     * Sets the status bar color for API 21 and higher
     *
     * @param activity activity to set status bar color
     * @param color color to set to
     */
    public static void setStatusBarColor(AppCompatActivity activity, int color) {
        if (Build.VERSION.SDK_INT > 20) {
            if (color == -1) {
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            } else {
                activity.getWindow().setStatusBarColor(color);
            }
        }
    }

    /**
     * Gets icon based on file type
     *
     * @param name file name
     * @param project project name
     * @return drawable resource int
     */
    public static int getIcon(String name, String project) {
        switch (name.substring(name.lastIndexOf(".") + 1, name.length())) {
            case "html":
                return R.drawable.ic_html;
            case "css":
                return R.drawable.ic_css;
            case "js":
                return R.drawable.ic_js;
            case "woff":case "ttf":case "otf":case "woff2":case "fnt":
                return R.drawable.ic_font;
            default:
                if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + project, name))) {
                    return R.drawable.ic_image;
                } else {
                    return R.drawable.ic_file;
                }
        }
    }

    /**
     * Utility method to convert from dp to pixels
     *
     * @param context context to get resources
     * @param dp to convert
     * @return value in px
     */
    public static int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * Item decoration for projects view
     */
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
