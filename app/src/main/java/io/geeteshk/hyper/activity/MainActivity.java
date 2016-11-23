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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.git.Giiit;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.helper.Validator;

/**
 * Main activity to show all main content
 */
@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final int CHANGE_PIN = 101;
    /**
     * Intent code for selecting an icon
     */
    public static final int SELECT_ICON = 100;
    /**
     * Toolbar object for activity
     */
    private static Toolbar mToolbar;
    /**
     * Project related stuff
     */
    String[] mObjects;
    ArrayList mObjectsList;
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
        setTheme(Theme.getThemeInt(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mObjects = new File(Constants.HYPER_ROOT).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && !name.equals(".git") && Project.isValid(name);
            }
        });

        mLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (mObjects != null) {
            mObjectsList = new ArrayList<>(Arrays.asList(mObjects));
        } else {
            mObjectsList = new ArrayList<>();
        }
        
        Validator.removeBroken(mObjectsList);
        mProjectAdapter = new ProjectAdapter(this, mObjectsList, mLayout);
        mProjectsList = (RecyclerView) findViewById(R.id.project_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        mProjectsList.setLayoutManager(layoutManager);
        mProjectsList.addItemDecoration(new Decor.GridSpacingItemDecoration(2, Decor.dpToPx(this, 2), true));
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

                                RadioButton defaultIcon = (RadioButton) rootView.findViewById(R.id.default_icon);
                                RadioButton chooseIcon = (RadioButton) rootView.findViewById(R.id.choose_icon);
                                mIcon = (ImageView) rootView.findViewById(R.id.favicon_image);

                                nameLayout.getEditText().setText(Pref.get(MainActivity.this, "name", ""));
                                authorLayout.getEditText().setText(Pref.get(MainActivity.this, "author", ""));
                                descriptionLayout.getEditText().setText(Pref.get(MainActivity.this, "description", ""));
                                keywordsLayout.getEditText().setText(Pref.get(MainActivity.this, "keywords", ""));

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
                                        if (Validator.validate(MainActivity.this, nameLayout, authorLayout, descriptionLayout, keywordsLayout)) {
                                            Pref.store(MainActivity.this, "name", nameLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "author", authorLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "description", descriptionLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "keywords", keywordsLayout.getEditText().getText().toString());

                                            Project.generate(MainActivity.this, nameLayout.getEditText().getText().toString(), authorLayout.getEditText().getText().toString(), descriptionLayout.getEditText().getText().toString(), keywordsLayout.getEditText().getText().toString(), mStream, mProjectAdapter);
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

                                builder.setIcon(R.drawable.ic_action_clone);
                                builder.setView(cloneView);
                                builder.setPositiveButton("CLONE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        Giiit.clone(MainActivity.this, new File(Constants.HYPER_ROOT + File.separator + file.getText().toString()), mProjectAdapter, remote.getText().toString(), username.getText().toString(), password.getText().toString());
                                    }
                                });

                                builder.setNegativeButton(R.string.cancel, null);
                                builder.create().show();
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
                showSettings();
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
                        mIcon.setImageBitmap(Decor.decodeUri(MainActivity.this, selectedImage));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case CHANGE_PIN:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.new_pin);
                EditText editText = new EditText(MainActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setPadding(60, 14, 60, 24);
                final TextInputLayout layout = new TextInputLayout(MainActivity.this);
                layout.addView(editText);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(60, 16, 60, 16);
                layout.setLayoutParams(params);
                builder.setView(layout);
                builder.setPositiveButton(R.string.accept, null);
                final AppCompatDialog dialog = builder.create();
                dialog.show();

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        assert layout.getEditText() != null;
                        String newPin = layout.getEditText().getText().toString();
                        if (newPin.length() != 4) {
                            layout.setError(getString(R.string.pin_four_digits));
                        } else {
                            Pref.store(MainActivity.this, "pin", newPin);
                            dialog.dismiss();
                        }
                    }
                });

                break;
        }
    }

    public void showSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View rootView = inflater.inflate(R.layout.fragment_settings, null, false);

        final SwitchCompat darkTheme = (SwitchCompat) rootView.findViewById(R.id.dark_theme);
        darkTheme.setChecked(Pref.get(MainActivity.this, "dark_theme", false));

        RelativeLayout darkThemeLayout = (RelativeLayout) rootView.findViewById(R.id.dark_theme_layout);
        darkThemeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkTheme.setChecked(!darkTheme.isChecked());
            }
        });

        final TextView autoSave = (TextView) rootView.findViewById(R.id.auto_save_freq_text);
        autoSave.setText(String.valueOf(Pref.get(MainActivity.this, "auto_save_freq", 2)) + "s");
        AppCompatSeekBar seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.auto_save_freq);
        seekBar.setProgress(Pref.get(MainActivity.this, "auto_save_freq", 2) - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Pref.store(MainActivity.this, "auto_save_freq", progress + 1);
                autoSave.setText(String.valueOf(progress + 1) + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RelativeLayout disableFileLayout = (RelativeLayout) rootView.findViewById(R.id.disable_file_ending_warn_layout);
        final SwitchCompat disableFile = (SwitchCompat) rootView.findViewById(R.id.disable_file_ending_warn);
        disableFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Pref.store(MainActivity.this, "show_toast_file_ending", b);
            }
        });

        disableFileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableFile.setChecked(!disableFile.isChecked());
            }
        });

        builder.setIcon(R.drawable.ic_settings);
        builder.setTitle("Settings");
        builder.setView(rootView);


        final AlertDialog dialog = builder.create();
        dialog.show();

        darkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dialog.dismiss();
                Pref.store(MainActivity.this, "dark_theme", isChecked);
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mObjectsList = new ArrayList<>(Arrays.asList(mObjects));
        Validator.removeBroken(mObjectsList);
        for (Iterator iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!string.toLowerCase(Locale.getDefault()).startsWith(query)) {
                iterator.remove();
            }
        }

        mProjectAdapter = new ProjectAdapter(MainActivity.this, mObjectsList, mLayout);
        mProjectsList.setAdapter(mProjectAdapter);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mObjectsList = new ArrayList<>(Arrays.asList(mObjects));
        Validator.removeBroken(mObjectsList);
        for (Iterator iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!string.toLowerCase(Locale.getDefault()).startsWith(newText)) {
                iterator.remove();
            }
        }

        mProjectAdapter = new ProjectAdapter(MainActivity.this, mObjectsList, mLayout);
        mProjectsList.setAdapter(mProjectAdapter);
        return false;
    }

    private class CreateAdapter extends ArrayAdapter<String> {

        private Context mContext;

        public CreateAdapter(Context context, int resource) {
            super(context, resource);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView;

            if (convertView == null) {
                rootView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_list, parent, false);
            } else {
                rootView = convertView;
            }

            ImageView imageView = (ImageView) rootView.findViewById(R.id.dialog_list_item_image);
            TextView textView = (TextView) rootView.findViewById(R.id.dialog_list_item_text);

            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.ic_action_create);
                    textView.setText("Create a new project");
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.ic_action_clone);
                    textView.setText("Clone a repository");
                    break;
            }

            return rootView;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
