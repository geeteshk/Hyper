/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.LogsAdapter;
import io.geeteshk.hyper.helper.Hyperion;
import io.geeteshk.hyper.helper.Network;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Theme;

/**
 * Activity to test projects
 */
public class WebActivity extends AppCompatActivity {

    /**
     * WebView to display project
     */
    private WebView mWebView;

    /**
     * ArrayList for JavaScript logs
     */
    private ArrayList<String> mLogs;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLogs = new ArrayList<>();
        Network.setDrive(new Hyperion(getIntent().getStringExtra("name"), mLogs));
        setTheme(Theme.getThemeInt(this));
        super.onCreate(savedInstanceState);

        try {
            Network.getDrive().start();
        } catch (IOException e) {
            mLogs.add(e.toString());
        }

        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        if (Pref.get(this, "dark_theme", false)) {
            toolbar.setPopupTheme(R.style.Hyper_Dark);
        }

        toolbar.setTitle(getIntent().getStringExtra("name"));
        setSupportActionBar(toolbar);

        mWebView = (WebView) findViewById(R.id.web_view);
        assert mWebView != null;
        mWebView.getSettings().setJavaScriptEnabled(true);

        String url = getIntent().getStringExtra("url");
        if (Network.getDrive().wasStarted() && Network.getDrive().isAlive() && Network.getIpAddress() != null) {
            url = "http://" + Network.getIpAddress() + ":8080/index.html";
        }

        mWebView.loadUrl(url);
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

        getSupportActionBar().setSubtitle(url);
    }

    /**
     * Called when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Network.getDrive() != null) {
            Network.getDrive().stop();
        }
    }

    /**
     * Called when menu is created
     *
     * @param menu object that holds menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);
        return true;
    }

    /**
     * Called when menu item is selected
     *
     * @param item selected menu item
     * @return true if handled
     */
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
