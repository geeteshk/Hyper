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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
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

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.adapter.GitLogsAdapter;
import io.geeteshk.hyper.fragment.EditorFragment;
import io.geeteshk.hyper.fragment.ImageFragment;
import io.geeteshk.hyper.git.Giiit;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Jason;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.widget.DiffView;
import io.geeteshk.hyper.widget.holder.FileTreeHolder;

/**
 * Activity to work on selected project
 */
public class ProjectActivity extends AppCompatActivity {

    private static final String TAG = ProjectActivity.class.getSimpleName();

    private static final int VIEW_CODE = 99;

    /**
     * Intent code to import image
     */
    private static final int IMPORT_FILE = 101;

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
            mFiles.add(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "index.html");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSpinner = new Spinner(this);
        mFileAdapter = new FileAdapter(this, mFiles);
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
                if (node.isLeaf() && item.file.isFile()) {
                    if (mFiles.contains(item.file.getPath())) {
                        setFragment(item.file.getPath(), false);
                        mDrawerLayout.closeDrawers();
                    } else {
                        if (!Project.isBinaryFile(item.file)) {
                            setFragment(item.file.getPath(), true);
                            mDrawerLayout.closeDrawers();
                        } else if (Project.isImageFile(item.file)) {
                            setFragment(item.file.getPath(), true);
                            mDrawerLayout.closeDrawers();
                        } else {
                            Snackbar.make(mDrawerLayout, R.string.not_text_file, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        treeView.setDefaultNodeLongClickListener(new TreeNode.TreeNodeLongClickListener() {
            @Override
            public boolean onLongClick(final TreeNode node, Object value) {
                final FileTreeHolder.FileTreeItem item = (FileTreeHolder.FileTreeItem) value;
                switch (item.file.getName()) {
                    case "index.html":
                        return false;
                    default:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
                        builder.setTitle(getString(R.string.delete) + " " + item.file.getName() + "?");
                        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final boolean[] delete = {true, false};
                                final String file = item.file.getName();
                                final TreeNode parent = node.getParent();
                                treeView.removeNode(node);
                                removeFragment(item.file.getPath());

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
                                                if (item.file.isDirectory()) {
                                                    try {
                                                        FileUtils.deleteDirectory(item.file);
                                                    } catch (IOException e) {
                                                        Log.e(TAG, e.toString());
                                                    }
                                                } else {
                                                    item.file.delete();
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
        RelativeLayout headerBackground = (RelativeLayout) findViewById(R.id.header_background);
        ImageView headerIcon = (ImageView) findViewById(R.id.header_icon);
        TextView headerTitle = (TextView) findViewById(R.id.header_title);
        TextView headerDesc = (TextView) findViewById(R.id.header_desc);

        headerBackground.setBackgroundResource(MATERIAL_BACKGROUNDS[(int) (Math.random() * 8)]);
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
                TreeNode folderNode = new TreeNode(new FileTreeHolder.FileTreeItem(R.drawable.ic_folder, file, mDrawerLayout));
                setupFileTree(folderNode, file);
                root.addChild(folderNode);
            } else {
                TreeNode fileNode = new TreeNode(new FileTreeHolder.FileTreeItem(Decor.getIcon(file), file, mDrawerLayout));
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
        bundle.putString("location", title);
        if (Project.isImageFile(new File(title))) {
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
            canCommit = Giiit.canCommit(mDrawerLayout, mProjectFile);
            canCheckout = Giiit.canCheckout(mDrawerLayout, mProjectFile);
            hasRemotes = Giiit.getRemotes(mDrawerLayout, mProjectFile) != null &&
                    Giiit.getRemotes(mDrawerLayout, mProjectFile).size() > 0;
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
                viewIntent.putExtra("html_path", mFiles.get(mSpinner.getSelectedItemPosition()));
                startActivityForResult(viewIntent, VIEW_CODE);
                return true;
            case R.id.action_import_file:
                Intent fontIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fontIntent.setType("file/*");
                if (fontIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(fontIntent, IMPORT_FILE);
                }
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_git_init:
                Giiit.init(ProjectActivity.this, mProjectFile, mDrawerLayout);
                return true;
            case R.id.action_git_add:
                Giiit.add(mDrawerLayout, mProjectFile);
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

                    }
                });
                gitCommitBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                final AlertDialog dialog4 = gitCommitBuilder.create();
                dialog4.show();
                dialog4.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!editText4.getText().toString().isEmpty()) {
                            Giiit.commit(ProjectActivity.this, mDrawerLayout, mProjectFile, editText4.getText().toString());
                            dialog4.dismiss();
                        } else {
                            editText4.setError(getString(R.string.commit_message_empty));
                        }
                    }
                });
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

                spinner.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, Giiit.getRemotes(mDrawerLayout, mProjectFile)));
                gitPushBuilder.setView(pushView);
                gitPushBuilder.setPositiveButton("PUSH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Giiit.push(ProjectActivity.this, mDrawerLayout, mProjectFile, (String) spinner.getSelectedItem(), new boolean[]{dryRun.isChecked(), force.isChecked(), thin.isChecked(), tags.isChecked()}, pushUsername.getText().toString(), pushPassword.getText().toString());
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

                spinner1.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, Giiit.getRemotes(mDrawerLayout, mProjectFile)));
                gitPullBuilder.setView(pullView);
                gitPullBuilder.setPositiveButton("PULL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Giiit.pull(ProjectActivity.this, mDrawerLayout, mProjectFile, (String) spinner1.getSelectedItem(), pullUsername.getText().toString(), pullPassword.getText().toString());
                    }
                });

                gitPullBuilder.setNegativeButton(R.string.cancel, null);
                gitPullBuilder.create().show();
            case R.id.action_git_log:
                List<RevCommit> commits = Giiit.getCommits(mDrawerLayout, mProjectFile);
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
                final List<RevCommit> commitsToDiff = Giiit.getCommits(mDrawerLayout, mProjectFile);
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
                                dialogInterface.cancel();
                                chosen[1] = i;
                                AlertDialog.Builder diffBuilder = new AlertDialog.Builder(ProjectActivity.this);
                                SpannableString string = Giiit.diff(mDrawerLayout, mProjectFile, commitsToDiff.get(chosen[0]).getId(), commitsToDiff.get(chosen[1]).getId());
                                View rootView = inflater.inflate(R.layout.dialog_diff, null, false);
                                DiffView diffView = (DiffView) rootView.findViewById(R.id.diff_view);
                                diffView.setDiffText(string);
                                diffBuilder.setView(rootView);
                                diffBuilder.create().show();
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

                Giiit.status(mDrawerLayout, mProjectFile, conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders);

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

                    }
                });
                gitBranch.setNegativeButton(R.string.cancel, null);
                final AlertDialog dialog5 = gitBranch.create();
                dialog5.show();
                dialog5.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!editText5.getText().toString().isEmpty()) {
                            Giiit.createBranch(ProjectActivity.this, mDrawerLayout, mProjectFile, editText5.getText().toString(), checkBox.isChecked());
                            dialog5.dismiss();
                        } else {
                            editText5.setError(getString(R.string.branch_name_empty));
                        }
                    }
                });
                return true;
            case R.id.action_git_branch_remove:
                AlertDialog.Builder gitRemove = new AlertDialog.Builder(this);
                final List<Ref> branchesList = Giiit.getBranches(mDrawerLayout, mProjectFile);
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
                            Giiit.deleteBranch(mDrawerLayout, mProjectFile, toDelete.toArray(new String[toDelete.size()]));
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
                final List<Ref> branches = Giiit.getBranches(mDrawerLayout, mProjectFile);
                int checkedItem = -1;
                CharSequence[] items = new CharSequence[0];
                if (branches != null) {
                    items = new CharSequence[branches.size()];
                    for (int i = 0; i < items.length; i++) {
                        items[i] = branches.get(i).getName();
                    }
                }

                for (int i = 0; i < items.length; i++) {
                    String branch = Giiit.getCurrentBranch(mDrawerLayout, mProjectFile);
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
                        Giiit.checkout(ProjectActivity.this, mDrawerLayout, mProjectFile, branches.get(i).getName());
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
            case IMPORT_FILE:
                if (resultCode == RESULT_OK) {
                    final Uri fileUri = data.getData();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.name);
                    View view = LayoutInflater.from(ProjectActivity.this).inflate(R.layout.dialog_input_single, null, false);
                    final TextInputEditText editText = (TextInputEditText) view.findViewById(R.id.input_text);
                    editText.setHint(R.string.file_name);
                    builder.setView(editText);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.import_not_java, null);
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (editText.getText().toString().isEmpty()) {
                                editText.setError("Please enter a name");
                            } else {
                                dialog.dismiss();
                                if (Project.importFile(ProjectActivity.this, mProject, fileUri, editText.getText().toString())) {
                                    Snackbar.make(mDrawerLayout, R.string.file_success, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(mDrawerLayout, R.string.file_fail, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
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
