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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.CreateAdapter;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.git.Giiit;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.ResourceHelper;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.helper.ProjectManager;
import io.geeteshk.hyper.helper.Styles;
import io.geeteshk.hyper.helper.DataValidator;

/**
 * Main activity to show all main content
 */
@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    /**
     * Intent code for selecting an icon
     */
    public static final int SELECT_ICON = 100;

    public static final int SETTINGS_CODE = 101;

    /**
     * ProjectManager related stuff
     */
    String[] mObjects;
    ArrayList<String> mObjectsList;
    ProjectAdapter mProjectAdapter;
    RecyclerView mProjectsList;

    /**
     * InputStream to read image from strorage
     */
    InputStream mStream;
    ImageView mIcon;

    CoordinatorLayout mLayout;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Styles.getThemeInt(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mObjects = new File(Constants.HYPER_ROOT).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && !name.equals(".git") && ProjectManager.isValid(name);
            }
        });

        mLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (mObjects != null) {
            mObjectsList = new ArrayList<>(Arrays.asList(mObjects));
        } else {
            mObjectsList = new ArrayList<>();
        }
        
        DataValidator.removeBroken(mObjectsList);
        mProjectsList = (RecyclerView) findViewById(R.id.project_list);
        mProjectAdapter = new ProjectAdapter(this, mObjectsList, mLayout, mProjectsList);
        boolean orientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        int numColumns = 2;
        if (isTablet) {
            if (orientation) {
                numColumns = 6;
            } else {
                numColumns = 4;
            }
        } else {
            if (orientation) {
                numColumns = 3;
            }
        }

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        mProjectsList.setLayoutManager(layoutManager);
        mProjectsList.addItemDecoration(new ResourceHelper.GridSpacingItemDecoration(numColumns, ResourceHelper.dpToPx(this, 2), true));
        mProjectsList.setItemAnimator(new DefaultItemAnimator());
        mProjectsList.setAdapter(mProjectAdapter);

        final FloatingActionButton cloneButton = (FloatingActionButton) findViewById(R.id.fab_create);
        cloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(MainActivity.this);
                choiceBuilder.setTitle("Would you like to...");
                choiceBuilder.setAdapter(new CreateAdapter(MainActivity.this, R.layout.item_dialog_list), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                AlertDialog.Builder createBuilder = new AlertDialog.Builder(MainActivity.this);
                                createBuilder.setTitle("Create a new project");
                                View rootView = LayoutInflater.from(MainActivity.this)
                                        .inflate(R.layout.dialog_create, null, false);

                                final TextInputLayout nameLayout = (TextInputLayout) rootView.findViewById(R.id.name_layout);
                                final TextInputLayout authorLayout = (TextInputLayout) rootView.findViewById(R.id.author_layout);
                                final TextInputLayout descriptionLayout = (TextInputLayout) rootView.findViewById(R.id.description_layout);
                                final TextInputLayout keywordsLayout = (TextInputLayout) rootView.findViewById(R.id.keywords_layout);

                                final Spinner typeSpinner = (Spinner) rootView.findViewById(R.id.type_spinner);
                                typeSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, ProjectManager.TYPES));
                                typeSpinner.setSelection(Prefs.get(MainActivity.this, "type", 0));

                                RadioButton defaultIcon = (RadioButton) rootView.findViewById(R.id.default_icon);
                                RadioButton chooseIcon = (RadioButton) rootView.findViewById(R.id.choose_icon);
                                mIcon = (ImageView) rootView.findViewById(R.id.favicon_image);

                                nameLayout.getEditText().setText(Prefs.get(MainActivity.this, "name", ""));
                                authorLayout.getEditText().setText(Prefs.get(MainActivity.this, "author", ""));
                                descriptionLayout.getEditText().setText(Prefs.get(MainActivity.this, "description", ""));
                                keywordsLayout.getEditText().setText(Prefs.get(MainActivity.this, "keywords", ""));

                                defaultIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            mIcon.setImageResource(R.drawable.ic_launcher);
                                            mStream = null;
                                        }
                                    }
                                });

                                chooseIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, SELECT_ICON);
                                        }
                                    }
                                });

                                createBuilder.setIcon(R.drawable.ic_action_create);
                                createBuilder.setView(rootView);
                                createBuilder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                                createBuilder.setNegativeButton("CANCEL", null);
                                final AlertDialog dialog1 = createBuilder.create();
                                dialog1.show();

                                dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (DataValidator.validateCreate(MainActivity.this, nameLayout, authorLayout, descriptionLayout, keywordsLayout)) {
                                            Prefs.store(MainActivity.this, "name", nameLayout.getEditText().getText().toString());
                                            Prefs.store(MainActivity.this, "author", authorLayout.getEditText().getText().toString());
                                            Prefs.store(MainActivity.this, "description", descriptionLayout.getEditText().getText().toString());
                                            Prefs.store(MainActivity.this, "keywords", keywordsLayout.getEditText().getText().toString());
                                            Prefs.store(MainActivity.this, "type", typeSpinner.getSelectedItemPosition());

                                            ProjectManager.generate(MainActivity.this, nameLayout.getEditText().getText().toString(), authorLayout.getEditText().getText().toString(), descriptionLayout.getEditText().getText().toString(), keywordsLayout.getEditText().getText().toString(), mStream, mProjectAdapter, mLayout, typeSpinner.getSelectedItemPosition());
                                            dialog1.dismiss();
                                        }
                                    }
                                });
                                break;
                            case 1:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Clone a repository");

                                View cloneView = LayoutInflater.from(MainActivity.this)
                                        .inflate(R.layout.dialog_clone, null, false);

                                final TextInputEditText file = (TextInputEditText) cloneView.findViewById(R.id.clone_name);
                                final TextInputEditText remote = (TextInputEditText) cloneView.findViewById(R.id.clone_url);
                                final TextInputEditText username = (TextInputEditText) cloneView.findViewById(R.id.clone_username);
                                final TextInputEditText password = (TextInputEditText) cloneView.findViewById(R.id.clone_password);

                                file.setText(Prefs.get(MainActivity.this, "clone_name", ""));
                                remote.setText(Prefs.get(MainActivity.this, "remote", ""));

                                builder.setIcon(R.drawable.ic_action_clone);
                                builder.setView(cloneView);
                                builder.setPositiveButton("CLONE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });

                                builder.setNegativeButton(R.string.cancel, null);
                                final AlertDialog dialog2 = builder.create();
                                dialog2.show();
                                dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (DataValidator.validateClone(MainActivity.this, file, remote)) {
                                            String remoteStr = remote.getText().toString();
                                            if (!remoteStr.contains("://")) {
                                                remoteStr = "https://" + remoteStr;
                                            }

                                            Prefs.store(MainActivity.this, "clone_name", file.getText().toString());
                                            Prefs.store(MainActivity.this, "remote", remoteStr);

                                            Giiit.clone(MainActivity.this, mLayout, new File(Constants.HYPER_ROOT + File.separator + file.getText().toString()), mProjectAdapter, remoteStr, username.getText().toString(), password.getText().toString());
                                            dialog2.dismiss();
                                        }
                                    }
                                });

                                break;
                            case 2:
                                DialogProperties properties = new DialogProperties();
                                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                                properties.selection_type = DialogConfigs.DIR_SELECT;
                                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                                properties.extensions = null;

                                FilePickerDialog pickerDialog = new FilePickerDialog(MainActivity.this, properties);
                                pickerDialog.setTitle("Import a project");
                                pickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                                    @Override
                                    public void onSelectedFilePaths(final String[] files) {
                                        if (files.length > 0) {
                                            AlertDialog.Builder createBuilder = new AlertDialog.Builder(MainActivity.this);
                                            createBuilder.setTitle("Import an external project");
                                            View rootView = LayoutInflater.from(MainActivity.this)
                                                    .inflate(R.layout.dialog_import, null, false);

                                            final TextInputLayout nameLayout = (TextInputLayout) rootView.findViewById(R.id.name_layout);
                                            final TextInputLayout authorLayout = (TextInputLayout) rootView.findViewById(R.id.author_layout);
                                            final TextInputLayout descriptionLayout = (TextInputLayout) rootView.findViewById(R.id.description_layout);
                                            final TextInputLayout keywordsLayout = (TextInputLayout) rootView.findViewById(R.id.keywords_layout);

                                            final Spinner typeSpinner = (Spinner) rootView.findViewById(R.id.type_spinner);
                                            typeSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, ProjectManager.TYPES));
                                            typeSpinner.setSelection(Prefs.get(MainActivity.this, "type", 0));

                                            nameLayout.getEditText().setText(new File(files[0]).getName());
                                            authorLayout.getEditText().setText(Prefs.get(MainActivity.this, "author", ""));
                                            descriptionLayout.getEditText().setText(Prefs.get(MainActivity.this, "description", ""));
                                            keywordsLayout.getEditText().setText(Prefs.get(MainActivity.this, "keywords", ""));

                                            createBuilder.setIcon(R.drawable.ic_action_import);
                                            createBuilder.setView(rootView);
                                            createBuilder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });

                                            createBuilder.setNegativeButton("CANCEL", null);
                                            final AlertDialog dialog1 = createBuilder.create();
                                            dialog1.show();

                                            dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (DataValidator.validateCreate(MainActivity.this, nameLayout, authorLayout, descriptionLayout, keywordsLayout)) {
                                                        Prefs.store(MainActivity.this, "name", nameLayout.getEditText().getText().toString());
                                                        Prefs.store(MainActivity.this, "author", authorLayout.getEditText().getText().toString());
                                                        Prefs.store(MainActivity.this, "description", descriptionLayout.getEditText().getText().toString());
                                                        Prefs.store(MainActivity.this, "keywords", keywordsLayout.getEditText().getText().toString());
                                                        Prefs.store(MainActivity.this, "type", typeSpinner.getSelectedItemPosition());

                                                        ProjectManager._import(files[0], nameLayout.getEditText().getText().toString(), authorLayout.getEditText().getText().toString(), descriptionLayout.getEditText().getText().toString(), keywordsLayout.getEditText().getText().toString(), typeSpinner.getSelectedItemPosition(), mProjectAdapter, mLayout);
                                                        dialog1.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                                pickerDialog.show();
                                break;
                        }
                    }
                });

                choiceBuilder.create().show();
            }
        });

        mProjectsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    cloneButton.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && cloneButton.isShown()) cloneButton.hide();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SETTINGS_CODE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when returning from an external activity
     *
     * @param requestCode code used to request intent
     * @param resultCode code returned from activity
     * @param data data returned from activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_ICON:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = data.getData();
                        mStream = MainActivity.this.getContentResolver().openInputStream(selectedImage);
                        mIcon.setImageBitmap(ResourceHelper.decodeUri(MainActivity.this, selectedImage));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case SETTINGS_CODE:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mObjectsList = new ArrayList<>(Arrays.asList(mObjects));
        DataValidator.removeBroken(mObjectsList);
        for (Iterator iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!string.toLowerCase(Locale.getDefault()).startsWith(query)) {
                iterator.remove();
            }
        }

        mProjectAdapter = new ProjectAdapter(MainActivity.this, mObjectsList, mLayout, mProjectsList);
        mProjectsList.setAdapter(mProjectAdapter);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
