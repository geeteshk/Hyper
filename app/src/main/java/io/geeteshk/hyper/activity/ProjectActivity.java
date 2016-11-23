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
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.adapter.GitLogsAdapter;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.fragment.EditorFragment;
import io.geeteshk.hyper.fragment.ImageFragment;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.git.Giiit;
import io.geeteshk.hyper.helper.Jason;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.widget.DiffView;
import io.geeteshk.hyper.widget.FileTreeHolder;

/**
 * Activity to work on selected project
 */
public class ProjectActivity extends AppCompatActivity {


    private static final int VIEW_CODE = 99;

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

    /**
     * Currently open files
     */
    private ArrayList<String> mFiles;
    /**
     * Spinner and Adapter to handle files
     */
    private Spinner mSpinner;
    private ArrayAdapter<String> mFileAdapter;
    /**
     * Drawer related stuffs
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    /**
     * Project definitions
     */
    private String mProject;
    private File mProjectFile;

    private TreeNode rootNode;
    private AndroidTreeView treeView;

    private final int[] MATERIAL_BACKGROUNDS = {
            R.drawable.material_bg_1,
            R.drawable.material_bg_2,
            R.drawable.material_bg_3,
            R.drawable.material_bg_4,
            R.drawable.material_bg_5,
            R.drawable.material_bg_6,
            R.drawable.material_bg_7,
            R.drawable.material_bg_8
    };

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mProject = getIntent().getStringExtra("project");
        mProjectFile = new File(Constants.HYPER_ROOT + File.separator + mProject);
        setTheme(Theme.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        if (getIntent().hasExtra("files")) {
            mFiles = getIntent().getStringArrayListExtra("files");
        } else {
            mFiles = new ArrayList<>();
            mFiles.add("index.html");
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

        LinearLayout fileBrowser = (LinearLayout) findViewById(R.id.file_browser);
        rootNode = TreeNode.root();
        setupFileTree(rootNode, mProjectFile);
        treeView = new AndroidTreeView(ProjectActivity.this, rootNode);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultViewHolder(FileTreeHolder.class);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle);
        treeView.setDefaultNodeClickListener(new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {
                FileTreeHolder.FileTreeItem item = (FileTreeHolder.FileTreeItem) value;
                if (node.isLeaf()) {
                    if (mFiles.contains(item.path)) {
                        setFragment(item.path, false);
                        mDrawerLayout.closeDrawers();
                    } else {
                        if (!Project.isBinaryFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + item.path))) {
                            setFragment(item.path, true);
                            mDrawerLayout.closeDrawers();
                        } else if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + item.path))) {
                            setFragment(item.path, true);
                            mDrawerLayout.closeDrawers();
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.not_text_file, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        treeView.setDefaultNodeLongClickListener(new TreeNode.TreeNodeLongClickListener() {
            @Override
            public boolean onLongClick(final TreeNode node, Object value) {
                final FileTreeHolder.FileTreeItem item = (FileTreeHolder.FileTreeItem) value;
                switch (item.text) {
                    case "index.html":
                        return false;
                    default:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
                        builder.setTitle(getString(R.string.delete) + " " + item.text + "?");
                        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final boolean[] delete = {true, false};
                                final String file = item.text;
                                final TreeNode parent = node.getParent();
                                treeView.removeNode(node);
                                removeFragment(item.path);

                                final Snackbar snackbar = Snackbar.make(
                                        mDrawerLayout,
                                        "Deleted " + file + ".",
                                        Snackbar.LENGTH_LONG
                                );

                                snackbar.setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        delete[0] = false;
                                        snackbar.dismiss();
                                    }
                                });

                                snackbar.setCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        super.onDismissed(snackbar, event);
                                        if (!delete[1]) {
                                            if (delete[0]) {
                                                File toDel = new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + item.path);
                                                if (toDel.isDirectory()) {
                                                    Project.deleteDirectory(ProjectActivity.this, toDel);
                                                } else {
                                                    toDel.delete();
                                                }
                                            } else {
                                                treeView.addNode(parent, node);
                                            }

                                            delete[1] = true;
                                        }
                                    }
                                });

                                snackbar.show();
                            }
                        });

                        builder.setNegativeButton(R.string.cancel, null);
                        builder.show();
                        return true;
                }
            }
        });

        fileBrowser.addView(treeView.getView());

        Random random = new Random();
        RelativeLayout headerBackground = (RelativeLayout) findViewById(R.id.header_background);
        ImageView headerIcon = (ImageView) findViewById(R.id.header_icon);
        TextView headerTitle = (TextView) findViewById(R.id.header_title);
        TextView headerDesc = (TextView) findViewById(R.id.header_desc);

        headerBackground.setBackgroundResource(MATERIAL_BACKGROUNDS[random.nextInt((8 - 1) + 1) + 1]);
        headerIcon.setImageBitmap(Project.getFavicon(mProject));
        headerTitle.setText(Jason.getProjectProperty(mProject, "name"));
        headerDesc.setText(Jason.getProjectProperty(mProject, "description"));

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(mProject, Project.getFavicon(mProject));
            this.setTaskDescription(description);
        }
    }

    private void setupFileTree(TreeNode root, File f) {
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return !name.startsWith(".");
            }
        });

        for (File file : files) {
            if (file.isDirectory()) {
                TreeNode folderNode = new TreeNode(new FileTreeHolder.FileTreeItem(R.drawable.ic_folder, file.getName(), file.getPath().substring(file.getPath().indexOf(mProject) + mProject.length() + 1, file.getPath().length())));
                setupFileTree(folderNode, file);
                root.addChild(folderNode);
            } else {
                TreeNode fileNode = new TreeNode(new FileTreeHolder.FileTreeItem(Decor.getIcon(file.getPath().substring(file.getPath().indexOf(mProject) + mProject.length() + 1, file.getPath().length()), mProject), file.getName(), file.getPath().substring(file.getPath().indexOf(mProject) + mProject.length() + 1, file.getPath().length())));
                root.addChild(fileNode);
            }
        }
    }

    private void removeFragment(String file) {
        mFiles.remove(file);
        mFileAdapter.remove(file);
        mFileAdapter.notifyDataSetChanged();
    }

    /**
     * Open file when selected by setting the correct fragment
     *
     * @param file file to open
     * @param add whether to add to adapter
     */
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

    /**
     * Method to get the type of fragment dependent on the file type
     *
     * @param title file name
     * @return fragment to be committed
     */
    public Fragment getFragment(String title) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", mFileAdapter.getCount());
        bundle.putString("location", mProject + File.separator + title);
        if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + mProject, title))) {
            return Fragment.instantiate(this, ImageFragment.class.getName(), bundle);
        } else {
            return Fragment.instantiate(this, EditorFragment.class.getName(), bundle);
        }
    }

    /**
     * Used to enable/disable certain git functions
     * based on whether project is a git repo
     *
     * @param menu menu to work with
     * @return whether preparation is handled correctly
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isGitRepo = new File(mProjectFile, ".git").exists() && new File(mProjectFile, ".git").isDirectory();
        boolean canCommit = false;
        boolean canCheckout = false;
        boolean hasRemotes = false;
        boolean isHtml = ((String) mSpinner.getSelectedItem()).endsWith(".html");
        if (isGitRepo) {
            canCommit = Giiit.canCommit(ProjectActivity.this, mProjectFile);
            canCheckout = Giiit.canCheckout(ProjectActivity.this, mProjectFile);
            hasRemotes = Giiit.getRemotes(ProjectActivity.this, mProjectFile) != null &&
                    Giiit.getRemotes(ProjectActivity.this, mProjectFile).size() > 0;
        }

        menu.findItem(R.id.action_view).setEnabled(isHtml);
        menu.findItem(R.id.action_git_add).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_commit).setEnabled(canCommit);
        menu.findItem(R.id.action_git_push).setEnabled(hasRemotes);
        menu.findItem(R.id.action_git_pull).setEnabled(hasRemotes);
        menu.findItem(R.id.action_git_log).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_diff).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_status).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_branch).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_remote).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_branch_checkout).setEnabled(canCheckout);

        return true;
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
        final LayoutInflater inflater = LayoutInflater.from(ProjectActivity.this);
        switch (item.getItemId()) {
            case R.id.action_run:
                Intent runIntent = new Intent(ProjectActivity.this, WebActivity.class);
                runIntent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + mProject + File.separator + "index.html");
                runIntent.putExtra("name", mProject);
                startActivity(runIntent);
                return true;
            case R.id.action_view:
                Intent viewIntent = new Intent(ProjectActivity.this, ViewActivity.class);
                viewIntent.putExtra("project", mProject);
                viewIntent.putExtra("html_path", Constants.HYPER_ROOT + File.separator + mProject + File.separator + mSpinner.getSelectedItem());
                startActivityForResult(viewIntent, VIEW_CODE);
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
                            setupFileTree(rootNode, mProjectFile);
                            treeView.setRoot(rootNode);
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
                        createResourceDirExists("css");
                        if (!editText2.getText().toString().isEmpty() && Project.createFile(mProject, "css" + File.separator + editText2.getText().toString() + ".css", Project.STYLE)) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            setFragment("css" + File.separator + editText2.getText().toString() + ".css", true);
                            setupFileTree(rootNode, mProjectFile);
                            treeView.setRoot(rootNode);
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
                        createResourceDirExists("js");
                        if (!editText3.getText().toString().isEmpty() && Project.createFile(mProject, "js" + File.separator + editText3.getText().toString() + ".js", Project.MAIN)) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            setFragment("js" + File.separator + editText3.getText().toString() + ".js", true);
                            setupFileTree(rootNode, mProjectFile);
                            treeView.setRoot(rootNode);
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
            case R.id.action_about:
                showAbout();
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
            case R.id.action_git_push:
                AlertDialog.Builder gitPushBuilder = new AlertDialog.Builder(ProjectActivity.this);
                gitPushBuilder.setTitle("Push changes");

                View pushView = LayoutInflater.from(ProjectActivity.this)
                        .inflate(R.layout.dialog_push, null, false);

                final Spinner spinner = (Spinner) pushView.findViewById(R.id.remotes_spinner);
                final CheckBox dryRun = (CheckBox) pushView.findViewById(R.id.dry_run);
                final CheckBox force = (CheckBox) pushView.findViewById(R.id.force);
                final CheckBox thin = (CheckBox) pushView.findViewById(R.id.thin);
                final CheckBox tags = (CheckBox) pushView.findViewById(R.id.tags);

                final TextInputEditText pushUsername = (TextInputEditText) pushView.findViewById(R.id.push_username);
                final TextInputEditText pushPassword = (TextInputEditText) pushView.findViewById(R.id.push_password);

                spinner.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, Giiit.getRemotes(ProjectActivity.this, mProjectFile)));
                gitPushBuilder.setView(pushView);
                gitPushBuilder.setPositiveButton("PUSH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Giiit.push(ProjectActivity.this, mProjectFile, (String) spinner.getSelectedItem(), new boolean[]{dryRun.isChecked(), force.isChecked(), thin.isChecked(), tags.isChecked()}, pushUsername.getText().toString(), pushPassword.getText().toString());
                    }
                });

                gitPushBuilder.setNegativeButton(R.string.cancel, null);
                gitPushBuilder.create().show();
                return true;
            case R.id.action_git_pull:
                AlertDialog.Builder gitPullBuilder = new AlertDialog.Builder(ProjectActivity.this);
                gitPullBuilder.setTitle("Push changes");

                View pullView = LayoutInflater.from(ProjectActivity.this)
                        .inflate(R.layout.dialog_pull, null, false);

                final Spinner spinner1 = (Spinner) pullView.findViewById(R.id.remotes_spinner);

                final TextInputEditText pullUsername = (TextInputEditText) pullView.findViewById(R.id.pull_username);
                final TextInputEditText pullPassword = (TextInputEditText) pullView.findViewById(R.id.pull_password);

                spinner1.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, Giiit.getRemotes(ProjectActivity.this, mProjectFile)));
                gitPullBuilder.setView(pullView);
                gitPullBuilder.setPositiveButton("PULL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Giiit.pull(ProjectActivity.this, mProjectFile, (String) spinner1.getSelectedItem(), pullUsername.getText().toString(), pullPassword.getText().toString());
                    }
                });

                gitPullBuilder.setNegativeButton(R.string.cancel, null);
                gitPullBuilder.create().show();
            case R.id.action_git_log:
                List<RevCommit> commits = Giiit.getCommits(ProjectActivity.this, mProjectFile);
                @SuppressLint("InflateParams") View layoutLog = inflater.inflate(R.layout.sheet_logs, null);
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
            case R.id.action_git_diff:
                final int[] chosen = {-1, -1};
                final List<RevCommit> commitsToDiff = Giiit.getCommits(ProjectActivity.this, mProjectFile);
                final CharSequence[] commitNames = new CharSequence[commitsToDiff.size()];
                for (int i = 0; i < commitNames.length; i++) {
                    commitNames[i] = commitsToDiff.get(i).getShortMessage();
                }

                AlertDialog.Builder firstCommit = new AlertDialog.Builder(ProjectActivity.this);
                firstCommit.setTitle("Choose first commit");
                firstCommit.setSingleChoiceItems(commitNames, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        chosen[0] = i;
                        AlertDialog.Builder secondCommit = new AlertDialog.Builder(ProjectActivity.this);
                        secondCommit.setTitle("Choose second commit");
                        secondCommit.setSingleChoiceItems(commitNames, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == chosen[0]) {
                                    Toast.makeText(ProjectActivity.this, "You can't diff the same commit!", Toast.LENGTH_SHORT).show();
                                } else {
                                    dialogInterface.cancel();
                                    chosen[1] = i;
                                    AlertDialog.Builder diffBuilder = new AlertDialog.Builder(ProjectActivity.this);
                                    SpannableString string = Giiit.diff(ProjectActivity.this, mProjectFile, commitsToDiff.get(chosen[0]).getId(), commitsToDiff.get(chosen[1]).getId());
                                    View rootView = inflater.inflate(R.layout.dialog_diff, null, false);
                                    DiffView diffView = (DiffView) rootView.findViewById(R.id.diff_view);
                                    diffView.setDiffText(string);
                                    diffBuilder.setView(rootView);
                                    diffBuilder.create().show();
                                }
                            }
                        });

                        secondCommit.create().show();
                    }
                });

                firstCommit.create().show();
                return true;
            case R.id.action_git_status:
                @SuppressLint("InflateParams") View layoutStatus = inflater.inflate(R.layout.item_git_status, null);
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
                checkBox.setText(R.string.checkout);
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

                    final List<String> toDelete = new ArrayList<>();
                    gitRemove.setMultiChoiceItems(itemsMultiple, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            if (b) {
                                toDelete.add(itemsMultiple[i].toString());
                            } else {
                                toDelete.remove(itemsMultiple[i].toString());
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
                    String branch = Giiit.getCurrentBranch(ProjectActivity.this, mProjectFile);
                    if (branch != null) {
                        if (branch.equals(items[i])) {
                            checkedItem = i;
                        }
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
            case R.id.action_git_remote:
                Intent remoteIntent = new Intent(ProjectActivity.this, RemotesActivity.class);
                remoteIntent.putExtra("project_file", mProjectFile.getPath());
                startActivity(remoteIntent);
                return true;
        }

        return false;
    }

    private void createResourceDirExists(String res) {
        File resDir = new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + res);
        if (!resDir.exists() && !resDir.isDirectory()) {
            resDir.mkdirs();
        }
    }

    /**
     * Show file ending warning/message
     *
     * @param image whether to show warning or message
     */
    private void showToast(boolean image) {
        if (!image) {
            Toast.makeText(this, R.string.file_ending_warn, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.file_ending_warn_image, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle different results from intents
     *
     * @param requestCode code used to start intent
     * @param resultCode result given by intent
     * @param data data given by intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMPORT_IMAGE:
                if (resultCode == RESULT_OK) {
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
                            createResourceDirExists("images");
                            if (!editText.getText().toString().isEmpty() && Project.importImage(ProjectActivity.this, mProject, imageUri, editText.getText().toString())) {
                                Toast.makeText(ProjectActivity.this, R.string.image_success, Toast.LENGTH_SHORT).show();
                                setFragment(editText.getText().toString(), true);
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

                break;
            case IMPORT_FONT:
                if (resultCode == RESULT_OK) {
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
                            createResourceDirExists("fonts");
                            if (!editText.getText().toString().isEmpty() && Project.importFont(ProjectActivity.this, mProject, fontUri, editText.getText().toString())) {
                                Toast.makeText(ProjectActivity.this, R.string.font_success, Toast.LENGTH_SHORT).show();
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

                break;
            case IMPORT_CSS:
                if (resultCode == RESULT_OK) {
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
                            createResourceDirExists("css");
                            if (!editText.getText().toString().isEmpty() && Project.importCss(ProjectActivity.this, mProject, cssUri, editText.getText().toString() + ".css")) {
                                Toast.makeText(ProjectActivity.this, "Successfully imported CSS file.", Toast.LENGTH_SHORT).show();
                                setFragment("css" + File.separator + editText.getText().toString() + ".css", true);
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

                break;
            case IMPORT_JS:
                if (resultCode == RESULT_OK) {
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
                            createResourceDirExists("js");
                            if (!editText.getText().toString().isEmpty() && Project.importJs(ProjectActivity.this, mProject, jsUri, editText.getText().toString() + ".js")) {
                                Toast.makeText(ProjectActivity.this, R.string.js_success, Toast.LENGTH_SHORT).show();
                                setFragment("js" + File.separator + editText.getText().toString() + ".js", true);
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

                break;
            case VIEW_CODE:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(ProjectActivity.this, ProjectActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.addFlags(getIntent().getFlags());
                    intent.putStringArrayListExtra("files", mFiles);
                    startActivity(intent);
                    finish();
                }

                break;
        }

        setupFileTree(rootNode, mProjectFile);
        treeView.setRoot(rootNode);
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

        name.setText(Jason.getProjectProperty(mProject, "name"));
        author.setText(Jason.getProjectProperty(mProject, "author"));
        description.setText(Jason.getProjectProperty(mProject, "description"));
        keywords.setText(Jason.getProjectProperty(mProject, "keywords"));

        if (Pref.get(this, "dark_theme", false)) {
            layout.setBackgroundColor(0xFF333333);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(layout);
        dialog.show();
    }
}
