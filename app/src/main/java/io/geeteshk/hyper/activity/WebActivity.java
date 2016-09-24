package io.geeteshk.hyper.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;

import java.util.ArrayList;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.LogsAdapter;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Jason;
import io.geeteshk.hyper.helper.Network;
import io.geeteshk.hyper.helper.Pref;

/**
 * Activity to test projects
 */
public class WebActivity extends AppCompatActivity {

    private WebView mWebView;
    private ArrayList<String> mLogs;

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Pref.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        if (Pref.get(this, "dark_theme", false)) {
            toolbar.setPopupTheme(R.style.Hyper_Dark);
        }

        toolbar.setTitle(getIntent().getStringExtra("name"));
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(Jason.getProjectProperty(getIntent().getStringExtra("name"), "color"))));

        int color = Color.parseColor(Jason.getProjectProperty(getIntent().getStringExtra("name"), "color"));
        if ((Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114) > 186) {
            getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#000000\">" + getIntent().getStringExtra("name") + "</font>")));
            PorterDuffColorFilter filter = new PorterDuffColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            Decor.setOverflowButtonColor(WebActivity.this, filter);
        } else {
            getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + getIntent().getStringExtra("name") + "</font>")));
        }

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        color = Color.HSVToColor(hsv);

        Decor.setStatusBarColor(this, -1);
        mLogs = new ArrayList<>();

        mWebView = (WebView) findViewById(R.id.web_view);
        assert mWebView != null;
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(getIntent().getStringExtra("url"));
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                mLogs.add(consoleMessage.message() + getString(R.string.from_line) + consoleMessage.lineNumber() + getString(R.string.of) + consoleMessage.sourceId());
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebView.animate().alpha(1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getIntent().getBooleanExtra("pilot", false) && Network.getDrive() != null) {
            Network.getDrive().stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);
        return true;
    }

    @SuppressLint("InflateParams")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = getLayoutInflater();
        switch (item.getItemId()) {
            case R.id.web_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra("url")));
                startActivity(intent);
                return true;
            case R.id.web_logs:
                View layoutLog = inflater.inflate(R.layout.sheet_logs, null);
                if (Pref.get(this, "dark_theme", false)) {
                    layoutLog.setBackgroundColor(0xFF333333);
                }

                RecyclerView logsList = (RecyclerView) layoutLog.findViewById(R.id.logs_list);
                RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
                RecyclerView.Adapter adapter = new LogsAdapter(mLogs);

                logsList.setLayoutManager(manager);
                logsList.setAdapter(adapter);

                BottomSheetDialog dialogLog = new BottomSheetDialog(this);
                dialogLog.setContentView(layoutLog);
                dialogLog.show();
                return true;
            case R.id.web_settings:
                View layout = inflater.inflate(R.layout.sheet_web_settings, null);
                if (Pref.get(this, "dark_theme", false)) {
                    layout.setBackgroundColor(0xFF333333);
                }

                SwitchCompat allowContentAccess, allowFileAccess, allowFileAccessFromFileURLs, allowUniversalAccessFromFileURLs, blockNetworkImage, blockNetworkLoads, builtInZoomControls, database, displayZoomControls, domStorage, jsCanOpenWindows, js, loadOverview, imageLoad, saveForm, wideView;
                allowContentAccess = (SwitchCompat) layout.findViewById(R.id.allow_content_access);
                allowFileAccess = (SwitchCompat) layout.findViewById(R.id.allow_file_access);
                allowFileAccessFromFileURLs = (SwitchCompat) layout.findViewById(R.id.allow_file_access_from_file_urls);
                allowUniversalAccessFromFileURLs = (SwitchCompat) layout.findViewById(R.id.allow_universal_access_from_file_urls);
                blockNetworkImage = (SwitchCompat) layout.findViewById(R.id.block_network_image);
                blockNetworkLoads = (SwitchCompat) layout.findViewById(R.id.block_network_loads);
                builtInZoomControls = (SwitchCompat) layout.findViewById(R.id.built_in_zoom_controls);
                database = (SwitchCompat) layout.findViewById(R.id.database_enabled);
                displayZoomControls = (SwitchCompat) layout.findViewById(R.id.display_zoom_controls);
                domStorage = (SwitchCompat) layout.findViewById(R.id.dom_storage_enabled);
                jsCanOpenWindows = (SwitchCompat) layout.findViewById(R.id.javascript_can_open_windows_automatically);
                js = (SwitchCompat) layout.findViewById(R.id.javascript_enabled);
                loadOverview = (SwitchCompat) layout.findViewById(R.id.load_with_overview_mode);
                imageLoad = (SwitchCompat) layout.findViewById(R.id.loads_images_automatically);
                saveForm = (SwitchCompat) layout.findViewById(R.id.save_form_data);
                wideView = (SwitchCompat) layout.findViewById(R.id.use_wide_view_port);

                allowContentAccess.setChecked(mWebView.getSettings().getAllowContentAccess());
                allowFileAccess.setChecked(mWebView.getSettings().getAllowFileAccess());
                allowFileAccessFromFileURLs.setChecked(mWebView.getSettings().getAllowFileAccessFromFileURLs());
                allowUniversalAccessFromFileURLs.setChecked(mWebView.getSettings().getAllowUniversalAccessFromFileURLs());
                blockNetworkImage.setChecked(mWebView.getSettings().getBlockNetworkImage());
                blockNetworkLoads.setChecked(mWebView.getSettings().getBlockNetworkLoads());
                builtInZoomControls.setChecked(mWebView.getSettings().getBuiltInZoomControls());
                database.setChecked(mWebView.getSettings().getDatabaseEnabled());
                displayZoomControls.setChecked(mWebView.getSettings().getDisplayZoomControls());
                domStorage.setChecked(mWebView.getSettings().getDomStorageEnabled());
                jsCanOpenWindows.setChecked(mWebView.getSettings().getJavaScriptCanOpenWindowsAutomatically());
                js.setChecked(mWebView.getSettings().getJavaScriptEnabled());
                loadOverview.setChecked(mWebView.getSettings().getLoadWithOverviewMode());
                imageLoad.setChecked(mWebView.getSettings().getLoadsImagesAutomatically());
                saveForm.setChecked(mWebView.getSettings().getSaveFormData());
                wideView.setChecked(mWebView.getSettings().getUseWideViewPort());

                allowContentAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setAllowContentAccess(isChecked);
                    }
                });

                allowFileAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setAllowFileAccess(isChecked);
                    }
                });

                allowFileAccessFromFileURLs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setAllowFileAccessFromFileURLs(isChecked);
                    }
                });

                allowUniversalAccessFromFileURLs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(isChecked);
                    }
                });

                blockNetworkImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setBlockNetworkImage(isChecked);
                    }
                });

                blockNetworkLoads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setBlockNetworkLoads(isChecked);
                    }
                });

                builtInZoomControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setBuiltInZoomControls(isChecked);
                    }
                });

                database.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setDatabaseEnabled(isChecked);
                    }
                });

                displayZoomControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setDisplayZoomControls(isChecked);
                    }
                });

                domStorage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setDomStorageEnabled(isChecked);
                    }
                });

                jsCanOpenWindows.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(isChecked);
                    }
                });

                js.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setJavaScriptEnabled(isChecked);
                    }
                });

                loadOverview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setLoadWithOverviewMode(isChecked);
                    }
                });

                imageLoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setLoadsImagesAutomatically(isChecked);
                    }
                });

                saveForm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setSaveFormData(isChecked);
                    }
                });

                wideView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mWebView.getSettings().setUseWideViewPort(isChecked);
                    }
                });

                BottomSheetDialog dialog = new BottomSheetDialog(this);
                dialog.setContentView(layout);
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
