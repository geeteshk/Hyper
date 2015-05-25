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
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.geeteshk.hyper.util.PreferenceUtil;

/**
 * Editor class to handle code highlighting etc
 */
public class Editor extends EditText {

    /**
     * Different colours for different parts of code
     */
    private static final int COLOR_KEYWORD = 0xfff92672;
    private static final int COLOR_PARAMS = 0xff64cbf4;
    private static final int COLOR_ENDING = 0xff9a79dd;
    private static final int COLOR_FUNCTIONS = 0xffed5c00;
    /**
     * Various patterns for detecting words in code
     */
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
                    "disabled|accesskey|tabindex|id|class)\\b"
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
    private static final Pattern COMMENTS = Pattern.compile(
            "/\\*(?:.|[\\n\\r])*?\\*/|<!--.*");
    private static final Pattern CSS_COMMENTS = Pattern.compile(
            "/\\*(?:.|[\\n\\r])*?\\*/|/\\*\\*.*");
    private static final Pattern JS_COMMENTS = Pattern.compile(
            "/\\*(?:.|[\\n\\r])*?\\*/|//.*");
    private static final Pattern ENDINGS = Pattern.compile(
            "(em|rem|px|pt|%)");
    private static final Pattern DATATYPES = Pattern.compile(
            "\\b(abstract|arguments|boolean|byte|char|class|" +
                    "const|double|enum|final|float|function|" +
                    "int|interface|long|native|package|" +
                    "private|protected|public|short|static|" +
                    "synchronized|transient|var|void|volatile)\\b");
    private static final Pattern SYMBOLS = Pattern.compile(
            "(&|=|throw|new|for|" +
                    "if|else|>|<|^|\\+|-|\\s\\|\\s|" +
                    "break|try|catch|finally|do|!|" +
                    "finally|default|case|switch|" +
                    "native|let|super|throws|return)");
    private static final Pattern FUNCTIONS = Pattern.compile("n\\((.*?)\\)");
    private static final Pattern NUMBERS = Pattern.compile("[-.0-9]+");
    private static final Pattern BOOLEANS = Pattern.compile("\\b(true|false)\\b");
    /**
     * Different colours for parts of code
     */
    private static int COLOR_BUILTIN = 0xffa6e22e;
    private static int COLOR_COMMENT = 0xff75715e;
    private static int COLOR_STRINGS = 0xffe6db74;
    /**
     * Handler used to update colours when code is changed
     */
    private final Handler mUpdateHandler = new Handler();
    /**
     * Custom listener
     */
    public OnTextChangedListener mOnTextChangedListener = null;
    /**
     * Delay used to update code
     */
    public int mUpdateDelay = 1000;
    /**
     * Type of code set
     */
    private CodeType mType;
    /**
     * Checks if code has been changed
     */
    private boolean mModified = true;
    /**
     * Runnable used to update colours when code is changed
     */
    private final Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable e = getText();
            if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(e.toString());
            highlightWithoutChange(e);
        }
    };
    /**
     * Context used to get preferences
     */
    private Context mContext;

    /**
     * Public constructor
     *
     * @param context used to get preferences
     */
    public Editor(Context context) {
        this(context, null);
    }

    /**
     * Public ocnstructor
     *
     * @param context used to get preferences
     * @param attrs   not used
     */
    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    /**
     * Highlights given code and sets it as text
     *
     * @param text to be highlighted
     */
    public void setTextHighlighted(CharSequence text) {
        cancelUpdate();

        mModified = false;
        setText(highlight(new SpannableStringBuilder(text)));
        mModified = true;

        if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(text.toString());
    }

    /**
     * Code used to initialise editor
     */
    private void init() {
        if (!PreferenceUtil.get(mContext, "dark_theme", false)) {
            COLOR_BUILTIN = 0xff72b000;
            COLOR_STRINGS = 0xffed5c00;
            COLOR_COMMENT = 0xffa0a0a0;
            setBackgroundColor(0xfff8f8f8);
            setTextColor(0xff222222);
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
                    if (c == '\n') return autoIndent(source, dest, dstart, dend);
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

                mUpdateHandler.postDelayed(mUpdateRunnable, mUpdateDelay);
            }
        });
    }

    /**
     * Prevent code from updating
     */
    private void cancelUpdate() {
        mUpdateHandler.removeCallbacks(mUpdateRunnable);
    }

    /**
     * Used in main runnable
     *
     * @param e text to be highlighted
     */
    private void highlightWithoutChange(Editable e) {
        mModified = false;
        highlight(e);
        mModified = true;
    }

    /**
     * Main method used for highlightin i.e. this is where the magic happens
     *
     * @param e text to be highlighted
     * @return highlighted text
     */
    private Editable highlight(Editable e) {
        try {
            clearSpans(e);

            if (e.length() == 0) return e;

            int counter;
            switch (mType) {
                case HTML:
                    for (Matcher m = KEYWORDS.matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.start() - 1) == '<' || e.toString().charAt(m.start() - 1) == '/') {
                            e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            e.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (Matcher m = BUILTINS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    counter = 0;
                    for (int index = e.toString().indexOf("\""); index >= 0; index = e.toString().indexOf("\"", index + 1)) {
                        if (counter % 2 == 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\"", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counter++;
                    }

                    for (Matcher m = COMMENTS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case CSS:
                    for (Matcher m = KEYWORDS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        e.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = PARAMS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_PARAMS), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf(":"); index >= 0; index = e.toString().indexOf(":", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(COLOR_ENDING), index + 1, e.toString().indexOf(";", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("."); index >= 0; index = e.toString().indexOf(".", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), index + 1, e.toString().indexOf("{", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("#"); index >= 0; index = e.toString().indexOf("#", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), index + 1, e.toString().indexOf("{", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = ENDINGS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    counter = 0;
                    for (int index = e.toString().indexOf("\""); index >= 0; index = e.toString().indexOf("\"", index + 1)) {
                        if (counter % 2 == 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\"", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counter++;
                    }

                    counter = 0;
                    for (int index = e.toString().indexOf("\'"); index >= 0; index = e.toString().indexOf("\'", index + 1)) {
                        if (counter % 2 == 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\'", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counter++;
                    }

                    for (Matcher m = CSS_COMMENTS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case JS:
                    for (Matcher m = DATATYPES.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_PARAMS), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        e.setSpan(new StyleSpan(Typeface.ITALIC), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = FUNCTIONS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_FUNCTIONS), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        e.setSpan(new StyleSpan(Typeface.ITALIC), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = SYMBOLS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("null"); index >= 0; index = e.toString().indexOf("null", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(COLOR_ENDING), index, index + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = NUMBERS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (Matcher m = BOOLEANS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_BUILTIN), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    counter = 0;
                    for (int index = e.toString().indexOf("\""); index >= 0; index = e.toString().indexOf("\"", index + 1)) {
                        if (counter % 2 == 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\"", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counter++;
                    }

                    counter = 0;
                    for (int index = e.toString().indexOf("\'"); index >= 0; index = e.toString().indexOf("\'", index + 1)) {
                        if (counter % 2 == 0) {
                            e.setSpan(new ForegroundColorSpan(COLOR_STRINGS), index, e.toString().indexOf("\'", index + 1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        counter++;
                    }

                    for (Matcher m = JS_COMMENTS.matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return e;
    }

    /**
     * Method to set the code type
     *
     * @param type of code
     */
    public void setType(CodeType type) {
        mType = type;
    }

    /**
     * Removes all spans
     *
     * @param e text to be cleared
     */
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

    /**
     * Method used for indenting code automatically
     *
     * @param source the main code
     * @param dest   the new code
     * @param dstart start of the code
     * @param dend   end of the code
     * @return indented code
     */
    private CharSequence autoIndent(CharSequence source, Spanned dest, int dstart, int dend) {
        String indent = "";
        int istart = dstart - 1;
        int iend;

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

                if (c == '(')
                    --pt;
                else if (c == ')')
                    ++pt;
            }
        }

        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);

            for (iend = ++istart;
                 iend < dend;
                 ++iend) {
                char c = dest.charAt(iend);

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

        if (pt < 0)
            indent += "\t";

        return source + indent;
    }

    /**
     * Determines the type of code that is being handled
     */
    public enum CodeType {
        HTML, CSS, JS
    }

    /**
     * Listens to when text is changed
     */
    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }
}
