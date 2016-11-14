package io.geeteshk.hyper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffView extends TextView {

    private static final Pattern INFO_CHANGES = Pattern.compile("/\\**?\\*/|diff.*");
    private static final Pattern INFO_CHANGES_TWO = Pattern.compile("/\\**?\\*/|index.*");
    private static final Pattern INFO_CHANGES_THREE = Pattern.compile("/\\**?\\*/|\\+\\+\\+.*");
    private static final Pattern INFO_CHANGES_FOUR = Pattern.compile("/\\**?\\*/|---.*");
    private static final Pattern LINE_CHANGES = Pattern.compile("/\\**?\\*/|@@.*");
    private static final Pattern ADD_CHANGES = Pattern.compile("/\\**?\\*/|\\+.*");
    private static final Pattern REMOVE_CHANGES = Pattern.compile("/\\**?\\*/|-.*");

    Paint mRectPaint;

    public DiffView(Context context) {
        this(context, null);
    }

    public DiffView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiffView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Consolas.ttf"));
        setBackgroundColor(0xff333333);
        setTextColor(0xffffffff);
        setTextSize(16);

        mRectPaint = new Paint();
        mRectPaint.setColor(0xff444444);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setAntiAlias(true);
    }

    public void setDiffText(SpannableString text) {
        setText(highlight(text));
    }

    public SpannableString highlight(SpannableString input) {
        for (Matcher m = REMOVE_CHANGES.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xfffc1501), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = ADD_CHANGES.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xff00ff66), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = LINE_CHANGES.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xffe066ff), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = INFO_CHANGES.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xffffe600), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = INFO_CHANGES_TWO.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xffffe600), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = INFO_CHANGES_THREE.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xffffe600), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = INFO_CHANGES_FOUR.matcher(input); m.find(); ) {
            input.setSpan(new ForegroundColorSpan(0xffffe600), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return input;
    }
}
