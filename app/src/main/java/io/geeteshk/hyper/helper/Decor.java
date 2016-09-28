package io.geeteshk.hyper.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import io.geeteshk.hyper.R;

public class Decor {

    public static void setStatusBarColor(AppCompatActivity activity, int color) {
        if (Build.VERSION.SDK_INT > 20) {
            if (color == -1) {
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            } else {
                activity.getWindow().setStatusBarColor(color);
            }
        }
    }

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

    public static int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static void setOverflowButtonColor(Context context, final PorterDuffColorFilter colorFilter) {
        final String overflowDescription = context.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                ImageView overflow = (ImageView) outViews.get(0);
                overflow.setColorFilter(colorFilter);
            }
        });
    }

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
