package io.geeteshk.hyper.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.AboutElementsAdapter;
import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.adapter.GitLogsAdapter;
import io.geeteshk.hyper.fragment.EditorFragment;
import io.geeteshk.hyper.fragment.ImageFragment;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.Giiit;
import io.geeteshk.hyper.helper.Hyperion;
import io.geeteshk.hyper.helper.Jason;
import io.geeteshk.hyper.helper.Network;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.polymer.CatalogActivity;
import io.geeteshk.hyper.polymer.Element;
import io.geeteshk.hyper.polymer.ElementsHolder;
import io.geeteshk.hyper.text.HtmlCompat;
import io.geeteshk.hyper.widget.KeyboardDetectorLayout;

/**
 * Activity to list projects
 */
public class ProjectActivity extends AppCompatActivity {

    public static final int FILES_CHANGED = 201;
    private static final String TAG = ProjectActivity.class.getSimpleName();
    /**
     * Intent code to import image
     */
    private static final int IMPORT_IMAGE = 101;
    /**
     * Intent code to import font
     */
    private static final int IMPORT_FONT = 102;
    /**
     * Intent code to import css
     */
    private static final int IMPORT_CSS = 103;
    /**
     * Intent code to import js
     */
    private static final int IMPORT_JS = 104;
    private static final int OPEN_RESOURCES = 105;
    private static final int POLYMER_ADD_CODE = 300;
    private List<String> mFiles;
    private Spinner mSpinner;
    private ArrayAdapter<String> mFileAdapter;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;

    private String mProject;

    private File mProjectFile;

    FirebaseAuth mAuth;
    FirebaseStorage mStorage;

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mProject = getIntent().getStringExtra("project");
        mProjectFile = new File(Constants.HYPER_ROOT + File.separator + mProject);
        if (Pref.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        Network.setDrive(new Hyperion(mProject));
        super.onCreate(savedInstanceState);

        try {
            Network.getDrive().start();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        KeyboardDetectorLayout layout = new KeyboardDetectorLayout(this, null);
        setContentView(layout);

        mFiles = new ArrayList<>();
        mFiles.add("index.html");
        mFiles.add("css/style.css");
        mFiles.add("js/main.js");

        RelativeLayout projectLayout = (RelativeLayout) findViewById(R.id.project_layout_snack);
        if (Pref.get(this, "pin", "").equals("")) {
            assert projectLayout != null;
            Snackbar snackbar = Snackbar.make(projectLayout, R.string.pin_snack_bar, Snackbar.LENGTH_LONG)
                    .setAction(R.string.set_pin, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setResult(1337);
                            finish();
                        }
                    });
            snackbar.show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSpinner = new Spinner(this);
        mFileAdapter = new FileAdapter(this, mProject, mFiles);
        mSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSpinner.setAdapter(mFileAdapter);
        toolbar.addView(mSpinner);
        if (Pref.get(this, "dark_theme", false)) {
            toolbar.setPopupTheme(R.style.Hyper_Dark);
        }

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_fragment, getFragment(mFiles.get(position)))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawer = (NavigationView) findViewById(R.id.drawer);
        setupMenu(mProject, null);

        View headerView = mDrawer.getHeaderView(0);
        RelativeLayout headerLayout = (RelativeLayout) headerView.findViewById(R.id.header_background);
        ImageView headerIcon = (ImageView) headerView.findViewById(R.id.header_icon);
        TextView headerTitle = (TextView) headerView.findViewById(R.id.header_title);
        TextView headerDesc = (TextView) headerView.findViewById(R.id.header_desc);

        headerLayout.setBackgroundColor(Color.parseColor(Jason.getProjectProperty(mProject, "color")));
        headerIcon.setImageBitmap(Project.getFavicon(mProject));
        headerTitle.setText(Jason.getProjectProperty(mProject, "name"));
        headerDesc.setText(Jason.getProjectProperty(mProject, "description"));

        mDrawer.setItemIconTintList(null);
        mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String file;
                if (item.getIntent() != null) {
                    file = item.getIntent().getStringExtra("location") + "/" + item.getTitle();
                } else {
                    file = item.getTitle().toString();
                }

                if (mFiles.contains(file)) {
                    setFragment(file, false);
                } else {
                    if (!Project.isBinaryFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + file))) {
                        setFragment(file, true);
                    } else if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + file))) {
                        setFragment(file, true);
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.not_text_file, Toast.LENGTH_SHORT).show();
                    }
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(mProject, Project.getFavicon(mProject), Color.parseColor(Jason.getProjectProperty(mProject, "color")));
            this.setTaskDescription(description);
        }
    }

    private void setFragment(String file, boolean add) {
        if (add) {
            mFileAdapter.add(file);
            mFileAdapter.notifyDataSetChanged();
        }

        mSpinner.setSelection(mFileAdapter.getPosition(file), true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.editor_fragment, getFragment(file))
                .commit();
    }

    public Fragment getFragment(String title) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", mFileAdapter.getCount());
        if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + mProject, title))) {
            ImageFragment imageFragment = (ImageFragment) Fragment.instantiate(this, ImageFragment.class.getName(), bundle);
            imageFragment.setProject(mProject);
            imageFragment.setFilename(title);
            return imageFragment;
        } else {
            EditorFragment editorFragment = (EditorFragment) Fragment.instantiate(this, EditorFragment.class.getName(), bundle);
            editorFragment.setProject(mProject);
            editorFragment.setFilename(title);
            return editorFragment;
        }
    }

    private void setupMenu(String project, @Nullable SubMenu menu) {
        File projectDir = new File(Constants.HYPER_ROOT + File.separator + project);
        File[] files = projectDir.listFiles();
        for (File file : files) {
            if (!file.getName().equals("bower_components") && !file.getName().equals(".git")) {
                if (file.isDirectory()) {
                    if (menu == null) {
                        setupMenu(project + File.separator + file.getName(), mDrawer.getMenu().addSubMenu(file.getName()));
                    } else {
                        setupMenu(project + File.separator + file.getName(), menu.addSubMenu(file.getName()));
                    }
                } else {
                    if (!file.getName().endsWith(".hyper")) {
                        if (menu == null) {
                            mDrawer.getMenu().add(file.getName()).setIcon(Decor.getIcon(file.getName(), mProject));
                        } else {
                            MenuItem item = menu.add(file.getName()).setIcon(Decor.getIcon(file.getName(), mProject));
                            Intent intent = new Intent();
                            intent.putExtra("location", menu.getItem().getTitle());
                            item.setIntent(intent);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isGitRepo = new File(mProjectFile, ".git").exists() && new File(mProjectFile, ".git").isDirectory();
        menu.findItem(R.id.action_git_add).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_commit).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_log).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_status).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_branch).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_clean).setEnabled(isGitRepo);

        return true;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    /**
     * Called after activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Called when config is changed
     *
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Network.getDrive() != null) {
            Network.getDrive().stop();
        }

        Firebase.updateProject(mAuth, mStorage, mProject, false);
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
        inflater.inflate(R.menu.menu_project, menu);
        return true;
    }

    /**
     * Called when menu item is selected
     *
     * @param item selected menu item
     * @return true if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = LayoutInflater.from(ProjectActivity.this);
        switch (item.getItemId()) {
            case R.id.action_run:
                Intent runIntent = new Intent(ProjectActivity.this, WebActivity.class);

                if (Network.getDrive().wasStarted() && Network.getDrive().isAlive() && Network.getIpAddress() != null) {
                    runIntent.putExtra("url", "http:///" + Network.getIpAddress() + ":8080");
                } else {
                    runIntent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + mProject + File.separator + "index.html");
                }

                runIntent.putExtra("name", mProject);
                startActivity(runIntent);
                return true;
            case R.id.action_import_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, IMPORT_IMAGE);
                }
                return true;
            case R.id.action_import_font:
                Intent fontIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fontIntent.setType("file/*");
                if (fontIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(fontIntent, IMPORT_FONT);
                }
                return true;
            case R.id.action_import_css:
                Intent cssIntent = new Intent(Intent.ACTION_GET_CONTENT);
                cssIntent.setType("text/css");
                if (cssIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cssIntent, IMPORT_CSS);
                }
                return true;
            case R.id.action_import_js:
                Intent jsIntent = new Intent(Intent.ACTION_GET_CONTENT);
                jsIntent.setType("text/javascript");
                if (jsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(jsIntent, IMPORT_JS);
                }
                return true;
            case R.id.action_create_html:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.new_not_java);
                final EditText editText = new EditText(this);
                editText.setHint(R.string.resource_name);
                editText.setSingleLine(true);
                editText.setMaxLines(1);
                builder.setView(editText);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty() && Project.createFile(mProject, editText.getText().toString() + ".html", Project.INDEX.replace("@name", Jason.getProjectProperty(mProject, "name")).replace("author", Jason.getProjectProperty(mProject, "author")).replace("@description", Jason.getProjectProperty(mProject, "description")).replace("@keywords", Jason.getProjectProperty(mProject, "keywords")).replace("@color", Jason.getProjectProperty(mProject, "color")))) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            setFragment(editText.getText().toString() + ".html", true);
                            refreshMenu();
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog = builder.create();
                if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                    showToast(false);
                dialog.show();
                return true;
            case R.id.action_create_css:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle(R.string.new_not_java);
                final EditText editText2 = new EditText(this);
                editText2.setHint(R.string.resource_name);
                editText2.setSingleLine(true);
                editText2.setMaxLines(1);
                builder2.setView(editText2);
                builder2.setCancelable(false);
                builder2.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText2.getText().toString().isEmpty() && Project.createFile(mProject, "css" + File.separator + editText2.getText().toString() + ".css", Project.STYLE)) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            setFragment("css" + File.separator + editText2.getText().toString() + ".css", true);
                            refreshMenu();
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog2 = builder2.create();
                if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                    showToast(false);
                dialog2.show();
                return true;
            case R.id.action_create_js:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setTitle(R.string.new_not_java);
                final EditText editText3 = new EditText(this);
                editText3.setHint(R.string.resource_name);
                editText3.setSingleLine(true);
                editText3.setMaxLines(1);
                builder3.setView(editText3);
                builder3.setCancelable(false);
                builder3.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText3.getText().toString().isEmpty() && Project.createFile(mProject, "js" + File.separator + editText3.getText().toString() + ".js", Project.MAIN)) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            setFragment("js" + File.separator + editText3.getText().toString() + ".js", true);
                            refreshMenu();
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder3.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog3 = builder3.create();
                if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                    showToast(false);
                dialog3.show();
                return true;
            case R.id.action_view_resources:
                Intent resourceIntent = new Intent(ProjectActivity.this, ResourcesActivity.class);
                resourceIntent.putExtra("project", mProject);
                startActivityForResult(resourceIntent, OPEN_RESOURCES);
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_polymer_add:
                Intent catalogIntent = new Intent(ProjectActivity.this, CatalogActivity.class);
                startActivityForResult(catalogIntent, POLYMER_ADD_CODE);
                ElementsHolder.getInstance().setProject(mProject);
                if (!new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "packages.hyper").exists()) {
                    ElementsHolder.getInstance().setElements(new ArrayList<Element>());
                } else {
                    ElementsHolder.getInstance().setElements(Jason.getPreviousElements(mProject));
                }

                return true;
            case R.id.action_git_init:
                Giiit.init(ProjectActivity.this, mProjectFile);
                return true;
            case R.id.action_git_add:
                Giiit.add(ProjectActivity.this, mProjectFile);
                return true;
            case R.id.action_git_commit:
                AlertDialog.Builder gitCommitBuilder = new AlertDialog.Builder(ProjectActivity.this);
                gitCommitBuilder.setTitle(R.string.git_commit);
                final EditText editText4 = new EditText(this);
                editText4.setHint(R.string.commit_message);
                gitCommitBuilder.setView(editText4);
                gitCommitBuilder.setCancelable(false);
                gitCommitBuilder.setPositiveButton(R.string.git_commit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText4.getText().toString().isEmpty()) {
                            Giiit.commit(ProjectActivity.this, mProjectFile, editText4.getText().toString());
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.commit_message_empty, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                gitCommitBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog4 = gitCommitBuilder.create();
                dialog4.show();
                return true;
            case R.id.action_git_log:
                List<RevCommit> commits = Giiit.getCommits(ProjectActivity.this, mProjectFile);
                View layoutLog = inflater.inflate(R.layout.sheet_logs, null);
                if (Pref.get(this, "dark_theme", false)) {
                    layoutLog.setBackgroundColor(0xFF333333);
                }

                RecyclerView logsList = (RecyclerView) layoutLog.findViewById(R.id.logs_list);
                RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
                RecyclerView.Adapter adapter = new GitLogsAdapter(ProjectActivity.this, commits);

                logsList.setLayoutManager(manager);
                logsList.setAdapter(adapter);

                BottomSheetDialog dialogLog = new BottomSheetDialog(this);
                dialogLog.setContentView(layoutLog);
                dialogLog.show();
                return true;
            case R.id.action_git_status:
                View layoutStatus = inflater.inflate(R.layout.item_git_status, null);
                if (Pref.get(this, "dark_theme", false)) {
                    layoutStatus.setBackgroundColor(0xFF333333);
                }

                TextView conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders;
                conflict = (TextView) layoutStatus.findViewById(R.id.status_conflicting);
                added = (TextView) layoutStatus.findViewById(R.id.status_added);
                changed = (TextView) layoutStatus.findViewById(R.id.status_changed);
                missing = (TextView) layoutStatus.findViewById(R.id.status_missing);
                modified = (TextView) layoutStatus.findViewById(R.id.status_modified);
                removed = (TextView) layoutStatus.findViewById(R.id.status_removed);
                uncommitted = (TextView) layoutStatus.findViewById(R.id.status_uncommitted);
                untracked = (TextView) layoutStatus.findViewById(R.id.status_untracked);
                untrackedFolders = (TextView) layoutStatus.findViewById(R.id.status_untracked_folders);

                Giiit.status(ProjectActivity.this, mProjectFile, conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders);

                BottomSheetDialog dialogStatus = new BottomSheetDialog(this);
                dialogStatus.setContentView(layoutStatus);
                dialogStatus.show();
                return true;
            case R.id.action_git_branch_new:
                AlertDialog.Builder gitBranch = new AlertDialog.Builder(ProjectActivity.this);
                gitBranch.setTitle("New branch");
                final EditText editText5 = new EditText(this);
                editText5.setHint("Branch name");
                LinearLayout layout = new LinearLayout(this);
                final CheckBox checkBox = new CheckBox(this);
                checkBox.setText("Checkout?");
                layout.addView(editText5);
                layout.addView(checkBox);
                gitBranch.setView(layout);
                gitBranch.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText5.getText().toString().isEmpty()) {
                            Giiit.createBranch(ProjectActivity.this, mProjectFile, editText5.getText().toString(), checkBox.isChecked());
                        } else {
                            Toast.makeText(ProjectActivity.this, "Please enter a branch name.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                gitBranch.setNegativeButton(R.string.cancel, null);
                AppCompatDialog dialog5 = gitBranch.create();
                dialog5.show();
                return true;
            case R.id.action_git_branch_remove:
                AlertDialog.Builder gitRemove = new AlertDialog.Builder(this);
                final List<Ref> branchesList = Giiit.getBranches(ProjectActivity.this, mProjectFile);
                if (branchesList != null) {
                    final CharSequence[] itemsMultiple = new CharSequence[branchesList.size()];
                    for (int i = 0; i < itemsMultiple.length; i++) {
                        itemsMultiple[i] = branchesList.get(i).getName();
                    }

                    final boolean[] checkedItems = new boolean[itemsMultiple.length];

                    final List<CharSequence> toDelete = new ArrayList<>();
                    gitRemove.setMultiChoiceItems(itemsMultiple, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            if (b) {
                                toDelete.add(itemsMultiple[i]);
                            } else {
                                toDelete.remove(itemsMultiple[i]);
                            }
                        }
                    });

                    gitRemove.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Giiit.deleteBranch(ProjectActivity.this, mProjectFile, toDelete.toArray(new String[toDelete.size()]));
                            dialogInterface.dismiss();
                        }
                    });
                }

                gitRemove.setNegativeButton(R.string.close, null);
                gitRemove.setTitle("Delete branches");
                gitRemove.create().show();
                return true;
            case R.id.action_git_branch_checkout:
                AlertDialog.Builder gitCheckout = new AlertDialog.Builder(this);
                final List<Ref> branches = Giiit.getBranches(ProjectActivity.this, mProjectFile);
                int checkedItem = -1;
                CharSequence[] items = new CharSequence[0];
                if (branches != null) {
                    items = new CharSequence[branches.size()];
                    for (int i = 0; i < items.length; i++) {
                        items[i] = branches.get(i).getName();
                    }
                }

                for (int i = 0; i < items.length; i++) {
                    if (Giiit.getCurrentBranch(ProjectActivity.this, mProjectFile).equals(items[i])) {
                        checkedItem = i;
                    }
                }

                gitCheckout.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        assert branches != null;
                        dialogInterface.dismiss();
                        Giiit.checkout(ProjectActivity.this, mProjectFile, branches.get(i).getName());
                    }
                });

                gitCheckout.setNegativeButton(R.string.close, null);
                gitCheckout.setTitle("Checkout branch");
                gitCheckout.create().show();
                return true;
            case R.id.action_git_clean:
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(ProjectActivity.this);
                confirmBuilder.setTitle("Are you sure you want to do this?");
                confirmBuilder.setMessage("Please use git status to check which files will be deleted. This may break your project if used incorrectly.");
                confirmBuilder.setPositiveButton(R.string.git_clean, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Set<String> cleaned = Giiit.clean(ProjectActivity.this, mProjectFile);
                        String[] cleanedArr = cleaned.toArray(new String[cleaned.size()]);
                        AlertDialog.Builder cleanBuilder = new AlertDialog.Builder(ProjectActivity.this);
                        cleanBuilder.setTitle("Cleaned files");
                        cleanBuilder.setItems(cleanedArr, null);
                        cleanBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                refreshMenu();
                            }
                        });
                        cleanBuilder.create().show();
                    }
                });
                confirmBuilder.setNegativeButton(R.string.cancel, null);
                confirmBuilder.create().show();
                return true;
        }

        return false;
    }

    private void showToast(boolean image) {
        if (!image) {
            Toast.makeText(this, R.string.file_ending_warn, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.file_ending_warn_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.name);
            final EditText editText = new EditText(this);
            editText.setHint(R.string.resource_name);
            editText.setSingleLine(true);
            editText.setMaxLines(1);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.import_not_java, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && Project.importImage(ProjectActivity.this, mProject, imageUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, R.string.image_success, Toast.LENGTH_SHORT).show();
                        setFragment(editText.getText().toString(), true);
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.image_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(true);
            dialog.show();
        }

        if (requestCode == IMPORT_FONT && resultCode == RESULT_OK) {
            final Uri fontUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.name);
            final EditText editText = new EditText(this);
            editText.setHint(R.string.resource_name);
            editText.setSingleLine(true);
            editText.setMaxLines(1);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.import_not_java, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && Project.importFont(ProjectActivity.this, mProject, fontUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, R.string.font_success, Toast.LENGTH_SHORT).show();
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.font_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(true);
            dialog.show();
        }

        if (requestCode == IMPORT_CSS && resultCode == RESULT_OK) {
            final Uri cssUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Name");
            final EditText editText = new EditText(this);
            editText.setHint("Resource name");
            editText.setSingleLine(true);
            editText.setMaxLines(1);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && Project.importCss(ProjectActivity.this, mProject, cssUri, editText.getText().toString() + ".css")) {
                        Toast.makeText(ProjectActivity.this, "Successfully imported CSS file.", Toast.LENGTH_SHORT).show();
                        setFragment("css" + File.separator + editText.getText().toString() + ".css", true);
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, "There was a problem while importing this CSS file.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(false);
            dialog.show();
        }

        if (requestCode == IMPORT_JS && resultCode == RESULT_OK) {
            final Uri jsUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.name);
            final EditText editText = new EditText(this);
            editText.setHint(R.string.resource_name);
            editText.setSingleLine(true);
            editText.setMaxLines(1);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.import_not_java, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && Project.importJs(ProjectActivity.this, mProject, jsUri, editText.getText().toString() + ".js")) {
                        Toast.makeText(ProjectActivity.this, R.string.js_success, Toast.LENGTH_SHORT).show();
                        setFragment("js" + File.separator + editText.getText().toString() + ".js", true);
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.js_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (Pref.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(false);
            dialog.show();
        }

        if (requestCode == OPEN_RESOURCES && resultCode == FILES_CHANGED) {
            refreshMenu();
        }

        if (requestCode == POLYMER_ADD_CODE) {
            refreshMenu();
        }
    }

    /**
     * Method to show about dialog holding project information
     */
    @SuppressLint("InflateParams")
    private void showAbout() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.sheet_about, null);

        TextView name = (TextView) layout.findViewById(R.id.project_name);
        TextView author = (TextView) layout.findViewById(R.id.project_author);
        TextView description = (TextView) layout.findViewById(R.id.project_description);
        TextView keywords = (TextView) layout.findViewById(R.id.project_keywords);
        TextView color = (TextView) layout.findViewById(R.id.project_color);

        RecyclerView elementsView = (RecyclerView) layout.findViewById(R.id.project_elements);
        RecyclerView.Adapter adapter = new AboutElementsAdapter(mProject);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);

        name.setText(Jason.getProjectProperty(mProject, "name"));
        author.setText(Jason.getProjectProperty(mProject, "author"));
        description.setText(Jason.getProjectProperty(mProject, "description"));
        keywords.setText(Jason.getProjectProperty(mProject, "keywords"));
        color.setText(Jason.getProjectProperty(mProject, "color"));
        color.setTextColor(Color.parseColor(Jason.getProjectProperty(mProject, "color")));

        elementsView.setAdapter(adapter);
        elementsView.setLayoutManager(manager);

        if (Pref.get(this, "dark_theme", false)) {
            layout.setBackgroundColor(0xFF333333);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(layout);
        dialog.show();
    }

    private void refreshMenu() {
        mDrawer.getMenu().clear();
        setupMenu(mProject, null);
    }
}
