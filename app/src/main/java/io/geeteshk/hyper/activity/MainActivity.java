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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.git.GitWrapper;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.DataValidator;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.helper.ProjectManager;
import io.geeteshk.hyper.helper.ResourceHelper;
import io.geeteshk.hyper.helper.Styles;

/**
 * Main activity to show all main content
 */
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Intent code for selecting an icon
     */
    public static final int SELECT_ICON = 100;

    public static final int SETTINGS_CODE = 101;

    private static final int IMPORT_PROJECT = 102;

    /**
     * ProjectManager related stuff
     */
    String[] contents;
    ArrayList<String> contentsList;
    ProjectAdapter projectAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.project_list) RecyclerView projectsList;
    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.fab_create) FloatingActionButton cloneButton;

    /**
     * InputStream to read image from storage
     */
    InputStream imageStream;
    ImageView projectIcon;

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
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        contents = new File(Constants.HYPER_ROOT).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && !name.equals(".git") && ProjectManager.isValid(name);
            }
        });

        if (contents != null) {
            contentsList = new ArrayList<>(Arrays.asList(contents));
        } else {
            contentsList = new ArrayList<>();
        }
        
        DataValidator.removeBroken(contentsList);
        projectAdapter = new ProjectAdapter(this, contentsList, coordinatorLayout, projectsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(projectAdapter);
        cloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(MainActivity.this);
                choiceBuilder.setTitle("Would you like to...");
                String[] choices = {"Create a new project", "Clone a repository", "Import an external project"};
                choiceBuilder.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, choices), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                AlertDialog.Builder createBuilder = new AlertDialog.Builder(MainActivity.this);
                                createBuilder.setTitle("Create a new project");
                                View rootView = View.inflate(MainActivity.this, R.layout.dialog_create, null);

                                final TextInputLayout nameLayout = rootView.findViewById(R.id.name_layout);
                                final TextInputLayout authorLayout = rootView.findViewById(R.id.author_layout);
                                final TextInputLayout descriptionLayout = rootView.findViewById(R.id.description_layout);
                                final TextInputLayout keywordsLayout = rootView.findViewById(R.id.keywords_layout);

                                final Spinner typeSpinner = rootView.findViewById(R.id.type_spinner);
                                typeSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, ProjectManager.TYPES));
                                typeSpinner.setSelection(Prefs.get(MainActivity.this, "type", 0));

                                RadioButton defaultIcon = rootView.findViewById(R.id.default_icon);
                                RadioButton chooseIcon = rootView.findViewById(R.id.choose_icon);
                                projectIcon = rootView.findViewById(R.id.favicon_image);

                                nameLayout.getEditText().setText(Prefs.get(MainActivity.this, "name", ""));
                                authorLayout.getEditText().setText(Prefs.get(MainActivity.this, "author", ""));
                                descriptionLayout.getEditText().setText(Prefs.get(MainActivity.this, "description", ""));
                                keywordsLayout.getEditText().setText(Prefs.get(MainActivity.this, "keywords", ""));

                                defaultIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            projectIcon.setImageResource(R.drawable.ic_launcher);
                                            imageStream = null;
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
                                            String name = nameLayout.getEditText().getText().toString();
                                            String author = authorLayout.getEditText().getText().toString();
                                            String description = descriptionLayout.getEditText().getText().toString();
                                            String keywords = keywordsLayout.getEditText().getText().toString();
                                            int type = typeSpinner.getSelectedItemPosition();

                                            Prefs.storeProject(MainActivity.this, name, author, description, keywords, type);
                                            ProjectManager.generate(
                                                    MainActivity.this,
                                                    name,
                                                    author,
                                                    description,
                                                    keywords,
                                                    imageStream,
                                                    projectAdapter,
                                                    coordinatorLayout,
                                                    type
                                            );

                                            dialog1.dismiss();
                                        }
                                    }
                                });
                                break;
                            case 1:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Clone a repository");

                                View cloneView = View.inflate(MainActivity.this, R.layout.dialog_clone, null);

                                final TextInputEditText file = cloneView.findViewById(R.id.clone_name);
                                final TextInputEditText remote = cloneView.findViewById(R.id.clone_url);
                                final TextInputEditText username = cloneView.findViewById(R.id.clone_username);
                                final TextInputEditText password = cloneView.findViewById(R.id.clone_password);

                                file.setText(Prefs.get(MainActivity.this, "clone_name", ""));
                                remote.setText(Prefs.get(MainActivity.this, "remote", ""));

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

                                            String cloneName = file.getText().toString();
                                            Prefs.store(MainActivity.this, "clone_name", cloneName);
                                            Prefs.store(MainActivity.this, "remote", remoteStr);

                                            GitWrapper.clone(
                                                    MainActivity.this,
                                                    coordinatorLayout,
                                                    new File(Constants.HYPER_ROOT + File.separator + cloneName),
                                                    projectAdapter,
                                                    remoteStr,
                                                    username.getText().toString(),
                                                    password.getText().toString()
                                            );

                                            dialog2.dismiss();
                                        }
                                    }
                                });

                                break;
                            case 2:
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("file/*");
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(intent, IMPORT_PROJECT);
                                }

                                break;
                        }
                    }
                });

                choiceBuilder.create().show();
            }
        });

        projectsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
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
                        if (selectedImage != null) {
                            imageStream = MainActivity.this.getContentResolver().openInputStream(selectedImage);
                            projectIcon.setImageBitmap(ResourceHelper.decodeUri(MainActivity.this, selectedImage));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }

                break;
            case SETTINGS_CODE:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case IMPORT_PROJECT:
                if (resultCode == RESULT_OK) {
                    Uri fileUri = data.getData();
                    if (fileUri != null) {
                        final File file = new File(fileUri.getPath());
                        AlertDialog.Builder createBuilder = new AlertDialog.Builder(MainActivity.this);
                        createBuilder.setTitle("Import an external project");
                        View rootView = View.inflate(MainActivity.this, R.layout.dialog_import, null);

                        final TextInputLayout nameLayout = rootView.findViewById(R.id.name_layout);
                        final TextInputLayout authorLayout = rootView.findViewById(R.id.author_layout);
                        final TextInputLayout descriptionLayout = rootView.findViewById(R.id.description_layout);
                        final TextInputLayout keywordsLayout = rootView.findViewById(R.id.keywords_layout);

                        final Spinner typeSpinner = rootView.findViewById(R.id.type_spinner);
                        typeSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, ProjectManager.TYPES));
                        typeSpinner.setSelection(Prefs.get(MainActivity.this, "type", 0));

                        nameLayout.getEditText().setText(file.getParentFile().getName());
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
                                    String name = nameLayout.getEditText().getText().toString();
                                    String author = authorLayout.getEditText().getText().toString();
                                    String description = descriptionLayout.getEditText().getText().toString();
                                    String keywords = keywordsLayout.getEditText().getText().toString();
                                    int type = typeSpinner.getSelectedItemPosition();

                                    Prefs.storeProject(MainActivity.this, name, author, description, keywords, type);
                                    ProjectManager._import(
                                            file.getParentFile().getPath(),
                                            name,
                                            author,
                                            description,
                                            keywords,
                                            type,
                                            projectAdapter,
                                            coordinatorLayout
                                    );

                                    dialog1.dismiss();
                                }
                            }
                        });
                    }
                }

                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        contentsList = new ArrayList<>(Arrays.asList(contents));
        DataValidator.removeBroken(contentsList);
        for (Iterator iterator = contentsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!string.toLowerCase(Locale.getDefault()).contains(newText)) {
                iterator.remove();
            }
        }

        projectAdapter = new ProjectAdapter(MainActivity.this, contentsList, coordinatorLayout, projectsList);
        projectsList.setAdapter(projectAdapter);
        return true;
    }

    @Override
    public boolean onClose() {
        contentsList = new ArrayList<>(Arrays.asList(contents));
        DataValidator.removeBroken(contentsList);
        projectAdapter = new ProjectAdapter(MainActivity.this, contentsList, coordinatorLayout, projectsList);
        projectsList.setAdapter(projectAdapter);
        return false;
    }
}
