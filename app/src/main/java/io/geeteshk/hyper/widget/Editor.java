package io.geeteshk.hyper.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.geeteshk.hyper.util.PreferenceUtil;

public class Editor extends EditText {

    public static final int TYPE_HTML = 0;
    public static final int TYPE_CSS = 1;
    public static final int TYPE_JS = 2;
    private static final int COLOR_KEYWORD = 0xfff92672;
    private static final Pattern KEYWORDS = Pattern.compile(
            "\\b(a|address|app|applet|area|b|" +
                    "base|basefont|bgsound|big|blink|blockquote|body|br|button|caption|center|cite|code|col|" +
                    "colgroup|comment|dd|del|dfn|dir|div|dl|dt|" +
                    "em|embed|fieldset|font|form|frame|frameset|h1|h2|h3|h4|" +
                    "h5|h6|head|hr|html|htmlplus|hype|i|iframe|img|input|ins|del|" +
                    "isindex|kbd|label|legend|li|link|listing|map|marquee|menu|meta|" +
                    "multicol|nobr|noembed|noframes|noscript|ol|option|p|param|plaintext|pre|s|" +
                    "samp|script|select|small|sound|spacer|span|strike|strong|style|sub|sup|table|tbody|td|" +
                    "textarea|tfoot|th|thead|title|tr|tt|u|var|wbr|xmp|import)\\b"
    );
    private static final Pattern BUILTINS = Pattern.compile(
            "\\b(charset|lang|href|name|target|onclick|onmouseover|onmouseout|accesskey|" +
                    "code|codebase|width|height|align|vspace|hspace|border|name|archive|mayscript|" +
                    "alt|shape|coords|target|nohref|size|color|face|src|" +
                    "loop|bgcolor|background|text|vlink|alink|" +
                    "bgproperties|topmargin|leftmargin|marginheight|marginwidth|onload|onunload|" +
                    "onfocus|onblur|stylesrc|scroll|clear|type|value|" +
                    "valign|span|compact|pluginspage|pluginurl|hidden|autostart|" +
                    "playcount|volume|controls|controller|mastersound|starttime|endtime|" +
                    "point-size|weight|action|method|enctype|onsubmit|onreset|" +
                    "scrolling|noresize|frameborder|bordercolor|cols|rows|framespacing|" +
                    "border|noshade|longdesc|ismap|usemap|lowsrc|naturalsizeflag|" +
                    "nosave|dynsrc|controls|start|suppress|maxlength|checked|language|" +
                    "onchange|onkeypress|onkeyup|onkeydown|autocomplete|prompt|" +
                    "for|rel|rev|media|direction|behaviour|scrolldelay|scrollamount|" +
                    "http-equiv|content|gutter|defer|event|multiple|readonly|cellpadding|" +
                    "cellspacing|rules|bordercolorlight|bordercolordark|summary|" +
                    "colspan|rowspan|nowrap|halign|" +
                    "disabled|accesskey|tabindex)\\b"
    );
    private static final Pattern PARAMS = Pattern.compile(
            "\\b(azimuth|background-attachment|background-color|" +
                    "background-image|background-position|background-repeat|" +
                    "background|border-collapse|border-color|" +
                    "border-spacing|border-style|border-top|border-right|" +
                    "border-bottom|border-left|border-top-color|" +
                    "border-right-color|border-left-color|border-bottom-color|" +
                    "border-top-style|border-right-style|border-bottom-style|" +
                    "border-left-style|border-top-width|border-right-width|" +
                    "border-bottom-width|border-left-width|border-width|" +
                    "border|bottom|caption-side|clear|clip|color|content|" +
                    "counter-increment|counter-reset|cue-after|cue-before|" +
                    "cue|cursor|direction|display|elevation|empty-cells|float|" +
                    "font-family|font-size|font-style|font-variant|font-weight|" +
                    "font|height|left|letter-spacing|line-height|list-style-image|" +
                    "list-style-position|list-style-type|list-style|margin-left|" +
                    "margin-right|margin-top|margin-bottom|margin|max-height|max-width|" +
                    "min-height|min-width|orphans|outline-color|outline-style|" +
                    "outline-width|outline|overflow|padding-top|padding-right|" +
                    "padding-bottom|padding-left|padding|page-break-after|" +
                    "page-break-before|page-break-inside|pause-after|pause-before|" +
                    "pause|pitch-range|pitch|play-during|position|quotes|richness|right|" +
                    "speak-header|speak-numeral|speak-punctuation|speak|speech-rate|" +
                    "stress|table-layout|text-align|text-decoration|text-indent|" +
                    "text-transform|top|unicode-bidi|vertical-align|visibility|" +
                    "voice-family|volume|white-space|widows|width|word-spacing|" +
                    "z-index)\\b");
    private static final Pattern COMMENTS = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|<!--.*");
    private static final Pattern TRAILING_WHITE_SPACE = Pattern.compile("[\\t ]+$", Pattern.MULTILINE);
    private static final int COLOR_PARAMS = 0xff64cbf4;
    private static int COLOR_BUILTIN = 0xffa6e22e;
    private static int COLOR_COMMENT = 0xff75715e;
    private static int COLOR_STRINGS = 0xffe6db74;
    private final Handler mUpdateHandler = new Handler();
    public OnTextChangedListener mOnTextChangedListener = null;
    public int mUpdateDelay = 1000;
    public int mErrorLine = 0;
    public boolean mDirty = false;
    private int mType = 0;
    private boolean mModified = true;
    private final Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable e = getText();
            if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(e.toString());
            highlightWithoutChange(e);
        }
    };
    private Context mContext;

    public Editor(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void setTextHighlighted(CharSequence text) {
        cancelUpdate();

        mErrorLine = 0;
        mDirty = false;

        mModified = false;
        setText(highlight(new SpannableStringBuilder(text)));
        mModified = true;

        if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(text.toString());
    }

    public String getCleanText() {
        return TRAILING_WHITE_SPACE.matcher(getText()).replaceAll("");
    }

    public void refresh() {
        highlightWithoutChange(getText());
    }

    private void init() {
        if (!PreferenceUtil.get(mContext, "dark_theme", false)) {
            COLOR_BUILTIN = 0xff72b000;
            COLOR_STRINGS = 0xffed5c00;
            COLOR_COMMENT = 0xffb8b8b8;
            setBackgroundColor(0xffffffff);
            setTextColor(0xff000000);
        } else {
            COLOR_BUILTIN = 0xffa6e22e;
            COLOR_COMMENT = 0xff75715e;
            COLOR_STRINGS = 0xffe6db74;
            setBackgroundColor(0xff222222);
            setTextColor(0xfff8f8f8);
        }

        setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/DroidSansMono.ttf"));
        setHorizontallyScrolling(true);

        setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (mModified && end - start == 1 && start < source.length() && dstart < dest.length()) {
                    char c = source.charAt(start);
                    if (c == '\n') return autoIndent(source, start, end, dest, dstart, dend);
                }

                return source;
            }
        }});

        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                cancelUpdate();

                if (!mModified) return;

                mDirty = true;
                mUpdateHandler.postDelayed(mUpdateRunnable, mUpdateDelay);
            }
        });
    }

    private void cancelUpdate() {
        mUpdateHandler.removeCallbacks(mUpdateRunnable);
    }

    private void highlightWithoutChange(Editable e) {
        mModified = false;
        highlight(e);
        mModified = true;
    }

    private Editable highlight(Editable e) {
        try {
            clearSpans(e);

            if (e.length() == 0) return e;

            switch (mType) {
                case TYPE_HTML:
                    for (Matcher m = KEYWORDS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = BUILTINS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = COMMENTS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    int counter = 1;
                    for (int index = e.toString().indexOf("\""); index >= 0; index = e.toString().indexOf("\"", index + 1)) {
                        if (counter % 2 != 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\"", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counter++;
                    }
                    break;
                case TYPE_CSS:
                    for (Matcher m = KEYWORDS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = PARAMS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_PARAMS), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf(":"); index >= 0; index = e.toString().indexOf(":", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), index + 1, e.toString().indexOf(";", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    int counterCss = 1;
                    for (int index = e.toString().indexOf("\""); index >= 0; index = e.toString().indexOf("\"", index + 1)) {
                        if (counterCss % 2 != 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\"", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counterCss++;
                    }

                    int counterCssTwo = 1;
                    for (int index = e.toString().indexOf("\'"); index >= 0; index = e.toString().indexOf("\'", index + 1)) {
                        if (counterCssTwo % 2 != 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\'", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counterCssTwo++;
                    }
                    break;
                case TYPE_JS:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return e;
    }

    public void setType(int type) {
        mType = type;
    }

    private void clearSpans(Editable e) {
        {
            ForegroundColorSpan spans[] = e.getSpans(0, e.length(), ForegroundColorSpan.class);
            for (int n = spans.length; n-- > 0; ) e.removeSpan(spans[n]);
        }

        {
            BackgroundColorSpan spans[] = e.getSpans(0, e.length(), BackgroundColorSpan.class);
            for (int n = spans.length; n-- > 0; ) e.removeSpan(spans[n]);
        }
    }

    private CharSequence autoIndent(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String indent = "";
        int istart = dstart - 1;
        int iend = -1;

        boolean dataBefore = false;
        int pt = 0;

        for (; istart > -1; --istart) {
            char c = dest.charAt(istart);

            if (c == '\n')
                break;

            if (c != ' ' &&
                    c != '\t') {
                if (!dataBefore) {
                    if (c == '{' ||
                            c == '+' ||
                            c == '-' ||
                            c == '*' ||
                            c == '/' ||
                            c == '%' ||
                            c == '^' ||
                            c == '=')
                        --pt;

                    dataBefore = true;
                }

                // parenthesis counter
                if (c == '(')
                    --pt;
                else if (c == ')')
                    ++pt;
            }
        }

        // copy indent of this line into the next
        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);

            for (iend = ++istart;
                 iend < dend;
                 ++iend) {
                char c = dest.charAt(iend);

                // auto expand comments
                if (charAtCursor != '\n' &&
                        c == '/' &&
                        iend + 1 < dend &&
                        dest.charAt(iend) == c) {
                    iend += 2;
                    break;
                }

                if (c != ' ' &&
                        c != '\t')
                    break;
            }

            indent += dest.subSequence(istart, iend);
        }

        // add new indent
        if (pt < 0)
            indent += "\t";

        // append white space of previous line and new indent
        return source + indent;
    }

    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }
}
