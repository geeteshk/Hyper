package io.geeteshk.hyper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

/**
 * Activity to test projects
 */
public class WebActivity extends AppCompatActivity {

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(getIntent().getStringExtra("name"));
        setSupportActionBar(toolbar);

        WebView webView = (WebView) findViewById(R.id.web_view);
        assert webView != null;
        webView.loadUrl(getIntent().getStringExtra("url"));
    }
}
