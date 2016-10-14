package io.geeteshk.hyper.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.ChromaUtil;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.FAQAdapter;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.FirstAid;
import io.geeteshk.hyper.helper.Giiit;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.helper.Validator;

/**
 * Main activity to show all main content
 */
@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity {

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
    ArrayList mObjectsList;
    ProjectAdapter mProjectAdapter;
    /**
     * Firebase class(es) to get user information
     * and perform specific Firebase functions
     */
    FirebaseAuth mAuth;
    FirebaseStorage mStorage;
    /**
     * InputStream to read image from strorage
     */
    InputStream mStream;
    ImageView mIcon;
    /**
     * Listener class to handle connection changes
     * or sudden sign-in state changes
     */
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    };

    /**
     * Used to change theme of application
     *
     * @param activity that is being changed
     * @param dark     theme
     */
    public void changeTheme(AppCompatActivity activity, boolean dark) {
        Pref.store(activity, "dark_theme", dark);
        activity.recreate();
    }

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        setTheme(Theme.getThemeInt(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final String[] objects = new File(Constants.HYPER_ROOT).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && !name.equals(".git") && !FirstAid.isBroken(name);
            }
        });

        mObjectsList = new ArrayList<>(Arrays.asList(objects));
        Validator.removeBroken(mObjectsList);
        mProjectAdapter = new ProjectAdapter(this, (String[]) mObjectsList.toArray(new String[mObjectsList.size()]), mAuth, mStorage);
        final RecyclerView projectsList = (RecyclerView) findViewById(R.id.project_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new Decor.GridSpacingItemDecoration(2, Decor.dpToPx(this, 2), true));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(mProjectAdapter);

        final ProgressDialog cloneProgress = new ProgressDialog(this);
        cloneProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        cloneProgress.setTitle("Cloning repository");
        cloneProgress.setMax(100);
        cloneProgress.setProgress(0);
        cloneProgress.setCancelable(false);

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

                                mAuth = FirebaseAuth.getInstance();
                                mStorage = FirebaseStorage.getInstance();

                                final TextInputLayout nameLayout = (TextInputLayout) rootView.findViewById(R.id.name_layout);
                                final TextInputLayout authorLayout = (TextInputLayout) rootView.findViewById(R.id.author_layout);
                                final TextInputLayout descriptionLayout = (TextInputLayout) rootView.findViewById(R.id.description_layout);
                                final TextInputLayout keywordsLayout = (TextInputLayout) rootView.findViewById(R.id.keywords_layout);

                                RadioButton defaultIcon = (RadioButton) rootView.findViewById(R.id.default_icon);
                                RadioButton chooseIcon = (RadioButton) rootView.findViewById(R.id.choose_icon);
                                mIcon = (ImageView) rootView.findViewById(R.id.favicon_image);
                                final TextView nColor = (TextView) rootView.findViewById(R.id.color);

                                nameLayout.getEditText().setText(Pref.get(MainActivity.this, "name", ""));
                                authorLayout.getEditText().setText(Pref.get(MainActivity.this, "author", ""));
                                descriptionLayout.getEditText().setText(Pref.get(MainActivity.this, "description", ""));
                                keywordsLayout.getEditText().setText(Pref.get(MainActivity.this, "keywords", ""));
                                nColor.setText(ChromaUtil.getFormattedColorString(Pref.get(MainActivity.this, "color", ContextCompat.getColor(MainActivity.this, R.color.colorAccent)), false));
                                nColor.setTextColor(Pref.get(MainActivity.this, "color", ContextCompat.getColor(MainActivity.this, R.color.colorAccent)));

                                defaultIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            mIcon.setImageResource(R.drawable.icon);
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

                                nColor.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new ChromaDialog.Builder()
                                                .initialColor(nColor.getCurrentTextColor())
                                                .colorMode(ColorMode.RGB)
                                                .indicatorMode(IndicatorMode.HEX)
                                                .onColorSelected(new OnColorSelectedListener() {
                                                    @Override
                                                    public void onColorSelected(@ColorInt int color) {
                                                        nColor.setText(ChromaUtil.getFormattedColorString(color, false));
                                                        nColor.setTextColor(color);
                                                    }
                                                })
                                                .create()
                                                .show(MainActivity.this.getSupportFragmentManager(), "Choose a colour for this project");
                                    }
                                });

                                createBuilder.setIcon(R.drawable.ic_action_create);
                                createBuilder.setView(rootView);
                                createBuilder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Validator.validate(MainActivity.this, nameLayout, authorLayout, descriptionLayout, keywordsLayout)) {
                                            Pref.store(MainActivity.this, "name", nameLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "author", authorLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "description", descriptionLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "keywords", keywordsLayout.getEditText().getText().toString());
                                            Pref.store(MainActivity.this, "color", nColor.getCurrentTextColor());

                                            Project.generate(MainActivity.this, nameLayout.getEditText().getText().toString(), authorLayout.getEditText().getText().toString(), descriptionLayout.getEditText().getText().toString(), keywordsLayout.getEditText().getText().toString(), nColor.getText().toString(), mStream);
                                            Firebase.uploadProject(mAuth, mStorage, nameLayout.getEditText().getText().toString(), false, true);
                                        }
                                    }
                                });

                                createBuilder.setNegativeButton("CANCEL", null);
                                createBuilder.create().show();
                                break;
                            case 1:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Clone a repository");

                                View cloneView = LayoutInflater.from(MainActivity.this)
                                        .inflate(R.layout.dialog_clone, null, false);

                                final TextInputEditText file = (TextInputEditText) cloneView.findViewById(R.id.clone_name);
                                final TextInputEditText remote = (TextInputEditText) cloneView.findViewById(R.id.clone_url);

                                builder.setIcon(R.drawable.ic_action_clone);
                                builder.setView(cloneView);
                                builder.setPositiveButton("CLONE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        cloneProgress.show();
                                        Giiit.clone(MainActivity.this, new File(Constants.HYPER_ROOT + File.separator + file.getText().toString()), cloneProgress, mProjectAdapter, remote.getText().toString());
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

        final EditText projectSearch = (EditText) findViewById(R.id.project_search);
        projectSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mObjectsList = new ArrayList<>(Arrays.asList(objects));
                Validator.removeBroken(mObjectsList);
                for (Iterator iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
                    String string = (String) iterator.next();
                    if (!string.toLowerCase(Locale.getDefault()).startsWith(s.toString())) {
                        iterator.remove();
                    }
                }

                mProjectAdapter = new ProjectAdapter(MainActivity.this, (String[]) mObjectsList.toArray(new String[mObjectsList.size()]), mAuth, mStorage);
                projectsList.setAdapter(mProjectAdapter);
            }
        });

        projectSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(projectSearch.getApplicationWindowToken(), 0);

                    return true;
                }

                return false;
            }
        });

        ImageButton clearSearch = (ImageButton) findViewById(R.id.clear_search);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                projectSearch.setText("");
            }
        });

        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        if (mObjectsList.isEmpty()) {
            projectsList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Firebase.syncProjects(MainActivity.this, mAuth, mStorage, layout);
            }
        });

        layout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Help");

                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View rootView = inflater.inflate(R.layout.fragment_help, null, false);

                RecyclerView faqList = (RecyclerView) rootView.findViewById(R.id.faq_list);
                RecyclerView.LayoutManager manager = new LinearLayoutManager(MainActivity.this);
                RecyclerView.Adapter adapter = new FAQAdapter(MainActivity.this);

                faqList.setLayoutManager(manager);
                faqList.setAdapter(adapter);

                builder.setIcon(R.drawable.ic_help);
                builder.setView(rootView);
                builder.create().show();
                return true;
            case R.id.action_license:
                final String name = "Hyper";
                final String url = "http://geeteshk.github.io/Hyper";
                final String copyright = "Copyright (c) 2016 Geetesh Kalakoti <kalakotig@gmail.com>";
                final License license = new MITLicense();
                final Notice notice = new Notice(name, url, copyright, license);
                new LicensesDialog.Builder(this)
                        .setNotices(notice)
                        .setThemeResourceId(Theme.getThemeInt(MainActivity.this))
                        .build()
                        .show();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when Activity is started to set listener
     */
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    /**
     * Called when Activity is stopped to remove listener
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
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

        TextView firebaseAccount = (TextView) rootView.findViewById(R.id.firebase_account);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            firebaseAccount.setText(user.getEmail());
        }

        RelativeLayout firebaseAccountLayout = (RelativeLayout) rootView.findViewById(R.id.firebase_account_layout);
        firebaseAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
            }
        });

        final SwitchCompat darkTheme = (SwitchCompat) rootView.findViewById(R.id.dark_theme);
        darkTheme.setChecked(Pref.get(MainActivity.this, "dark_theme", false));
        darkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeTheme(MainActivity.this, isChecked);
            }
        });

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

        RelativeLayout repairLayout = (RelativeLayout) rootView.findViewById(R.id.repair_layout);
        repairLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstAid.repairAll(MainActivity.this);
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
        builder.create().show();
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
