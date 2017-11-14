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
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.LogsAdapter;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.HyperServer;
import io.geeteshk.hyper.helper.NetworkUtils;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.helper.ProjectManager;
import io.geeteshk.hyper.helper.Styles;

/**
 * Activity to test projects
 */
public class WebActivity extends AppCompatActivity {

    private static final String TAG = WebActivity.class.getSimpleName();

    /**
     * WebView to display project
     */
    @BindView(R.id.web_view) WebView webView;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.loading_progress) ProgressBar loadingProgress;

    /**
     * ArrayList for JavaScript logs
     */
    private ArrayList<ConsoleMessage> jsLogs;

    String localUrl, localWithoutIndex;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String project = getIntent().getStringExtra("name");
        jsLogs = new ArrayList<>();
        NetworkUtils.setServer(new HyperServer(project));
        setTheme(Styles.getThemeInt(this));
        super.onCreate(savedInstanceState);

        try {
            NetworkUtils.getServer().start();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
        
        File indexFile = ProjectManager.getIndexFile(project);
        String indexStr = indexFile.getPath();
        indexStr = indexStr.replace(new File(Constants.HYPER_ROOT + File.separator + project).getPath(), "");

        toolbar.setTitle(project);
        setSupportActionBar(toolbar);
        webView.getSettings().setJavaScriptEnabled(true);
        localUrl = getIntent().getStringExtra("localUrl");
        if (NetworkUtils.getServer().wasStarted() && NetworkUtils.getServer().isAlive() && NetworkUtils.getIpAddress() != null) {
            localUrl = "http://" + NetworkUtils.getIpAddress() + ":8080" + indexStr;
        }

        localWithoutIndex = localUrl.substring(0, localUrl.length() - 10);
        webView.loadUrl(localUrl);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                loadingProgress.setProgress(newProgress);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                jsLogs.add(consoleMessage);
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(WebActivity.this)
                        .setTitle("Alert")
                        .setMessage(message)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                result.confirm();
                            }
                        })
                        .show();

                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(WebActivity.this)
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                result.confirm();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                result.cancel();
                            }
                        })
                        .show();

                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String localUrl) {
                super.onPageFinished(view, localUrl);
                webView.animate().alpha(1);
            }
        });

        toolbar.setSubtitle(localUrl);
    }

    /**
     * Called when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (NetworkUtils.getServer() != null) {
            NetworkUtils.getServer().stop();
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
            case R.id.refresh:
                webView.animate().alpha(0);
                webView.reload();
                return true;
            case R.id.web_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(localUrl));
                startActivity(intent);
                return true;
            case R.id.web_logs:
                View layoutLog = inflater.inflate(R.layout.sheet_logs, null);
                if (Prefs.get(this, "dark_theme", false)) {
                    layoutLog.setBackgroundColor(0xFF333333);
                }

                RecyclerView logsList = layoutLog.findViewById(R.id.logs_list);
                LinearLayoutManager manager = new LinearLayoutManager(this);
                RecyclerView.Adapter adapter = new LogsAdapter(WebActivity.this, localWithoutIndex, jsLogs);

                logsList.setLayoutManager(manager);
                logsList.addItemDecoration(new DividerItemDecoration(WebActivity.this, manager.getOrientation()));
                logsList.setAdapter(adapter);

                BottomSheetDialog dialogLog = new BottomSheetDialog(this);
                dialogLog.setContentView(layoutLog);
                dialogLog.show();
                return true;
            case R.id.web_settings:
                View layout = inflater.inflate(R.layout.sheet_web_settings, null);
                if (Prefs.get(this, "dark_theme", false)) {
                    layout.setBackgroundColor(0xFF333333);
                }

                SwitchCompat allowContentAccess, allowFileAccess, allowFileAccessFromFileURLs, allowUniversalAccessFromFileURLs, blockNetworkImage, blockNetworkLoads, builtInZoomControls, database, displayZoomControls, domStorage, jsCanOpenWindows, js, loadOverview, imageLoad, saveForm, wideView;
                allowContentAccess = layout.findViewById(R.id.allow_content_access);
                allowFileAccess = layout.findViewById(R.id.allow_file_access);
                allowFileAccessFromFileURLs = layout.findViewById(R.id.allow_file_access_from_file_urls);
                allowUniversalAccessFromFileURLs = layout.findViewById(R.id.allow_universal_access_from_file_urls);
                blockNetworkImage = layout.findViewById(R.id.block_network_image);
                blockNetworkLoads = layout.findViewById(R.id.block_network_loads);
                builtInZoomControls = layout.findViewById(R.id.built_in_zoom_controls);
                database = layout.findViewById(R.id.database_enabled);
                displayZoomControls = layout.findViewById(R.id.display_zoom_controls);
                domStorage = layout.findViewById(R.id.dom_storage_enabled);
                jsCanOpenWindows = layout.findViewById(R.id.javascript_can_open_windows_automatically);
                js = layout.findViewById(R.id.javascript_enabled);
                loadOverview = layout.findViewById(R.id.load_with_overview_mode);
                imageLoad = layout.findViewById(R.id.loads_images_automatically);
                saveForm = layout.findViewById(R.id.save_form_data);
                wideView = layout.findViewById(R.id.use_wide_view_port);

                allowContentAccess.setChecked(webView.getSettings().getAllowContentAccess());
                allowFileAccess.setChecked(webView.getSettings().getAllowFileAccess());
                blockNetworkImage.setChecked(webView.getSettings().getBlockNetworkImage());
                blockNetworkLoads.setChecked(webView.getSettings().getBlockNetworkLoads());
                builtInZoomControls.setChecked(webView.getSettings().getBuiltInZoomControls());
                database.setChecked(webView.getSettings().getDatabaseEnabled());
                displayZoomControls.setChecked(webView.getSettings().getDisplayZoomControls());
                domStorage.setChecked(webView.getSettings().getDomStorageEnabled());
                jsCanOpenWindows.setChecked(webView.getSettings().getJavaScriptCanOpenWindowsAutomatically());
                js.setChecked(webView.getSettings().getJavaScriptEnabled());
                loadOverview.setChecked(webView.getSettings().getLoadWithOverviewMode());
                imageLoad.setChecked(webView.getSettings().getLoadsImagesAutomatically());
                saveForm.setChecked(webView.getSettings().getSaveFormData());
                wideView.setChecked(webView.getSettings().getUseWideViewPort());

                allowContentAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setAllowContentAccess(isChecked);
                    }
                });

                allowFileAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setAllowFileAccess(isChecked);
                    }
                });

                blockNetworkImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setBlockNetworkImage(isChecked);
                    }
                });

                blockNetworkLoads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setBlockNetworkLoads(isChecked);
                    }
                });

                builtInZoomControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setBuiltInZoomControls(isChecked);
                    }
                });

                database.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setDatabaseEnabled(isChecked);
                    }
                });

                displayZoomControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setDisplayZoomControls(isChecked);
                    }
                });

                domStorage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setDomStorageEnabled(isChecked);
                    }
                });

                jsCanOpenWindows.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(isChecked);
                    }
                });

                js.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setJavaScriptEnabled(isChecked);
                    }
                });

                loadOverview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setLoadWithOverviewMode(isChecked);
                    }
                });

                imageLoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setLoadsImagesAutomatically(isChecked);
                    }
                });

                saveForm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setSaveFormData(isChecked);
                    }
                });

                wideView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        webView.getSettings().setUseWideViewPort(isChecked);
                    }
                });


                if (Build.VERSION.SDK_INT >= 16) {
                    allowFileAccessFromFileURLs.setChecked(webView.getSettings().getAllowFileAccessFromFileURLs());
                    allowUniversalAccessFromFileURLs.setChecked(webView.getSettings().getAllowUniversalAccessFromFileURLs());
                    allowFileAccessFromFileURLs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            webView.getSettings().setAllowFileAccessFromFileURLs(isChecked);
                        }
                    });

                    allowUniversalAccessFromFileURLs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            webView.getSettings().setAllowUniversalAccessFromFileURLs(isChecked);
                        }
                    });
                } else {
                    allowFileAccessFromFileURLs.setVisibility(View.GONE);
                    allowUniversalAccessFromFileURLs.setVisibility(View.GONE);
                }

                BottomSheetDialog dialog = new BottomSheetDialog(this);
                dialog.setContentView(layout);
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
