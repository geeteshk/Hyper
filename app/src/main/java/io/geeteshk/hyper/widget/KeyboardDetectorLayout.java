package io.geeteshk.hyper.widget;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import io.geeteshk.hyper.R;

public class KeyboardDetectorLayout extends RelativeLayout {

    Context mContext;

    public KeyboardDetectorLayout(Context context) {
        this(context, null);
    }

    public KeyboardDetectorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardDetectorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_project, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            if (actualHeight > proposedheight) {
                ((AppCompatActivity) mContext).getSupportActionBar().hide();
            } else if (actualHeight < proposedheight) {
                ((AppCompatActivity) mContext).getSupportActionBar().show();
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
