package io.geeteshk.hyper;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

import io.geeteshk.hyper.util.JsonUtil;
import io.geeteshk.hyper.util.NetworkUtil;

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
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(JsonUtil.getProjectProperty(getIntent().getStringExtra("name"), "color"))));


        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor(JsonUtil.getProjectProperty(getIntent().getStringExtra("name"), "color")));
        }

        WebView webView = (WebView) findViewById(R.id.web_view);
        assert webView != null;
        webView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getIntent().getBooleanExtra("pilot", false) && NetworkUtil.getDrive() != null) {
            NetworkUtil.getDrive().stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.web_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra("url")));
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
