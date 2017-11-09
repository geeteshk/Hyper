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
import android.support.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.adapter.GitLogsAdapter;
import io.geeteshk.hyper.fragment.EditorFragment;
import io.geeteshk.hyper.fragment.ImageFragment;
import io.geeteshk.hyper.git.GitWrapper;
import io.geeteshk.hyper.helper.Clipboard;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.HTMLParser;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.helper.ProjectManager;
import io.geeteshk.hyper.helper.ResourceHelper;
import io.geeteshk.hyper.helper.Styles;
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

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.file_browser) LinearLayout fileBrowser;
    
    /**
     * Currently open files
     */
    private ArrayList<String> openFiles;
    /**
     * Spinner and Adapter to handle files
     */
    private Spinner fileSpinner;
    private ArrayAdapter<String> fileAdapter;
    /**
     * Drawer related stuffs
     */
    private ActionBarDrawerToggle toggle;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    /**
     * ProjectManager definitions
     */
    private String projectName;
    private File projectDir, indexFile;

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

    private String[] props;
    @BindView(R.id.header_title) TextView headerTitle;
    @BindView(R.id.header_desc) TextView headerDesc;
    @BindView(R.id.header_background) RelativeLayout headerBackground;
    @BindView(R.id.header_icon) ImageView headerIcon;
    @BindView(R.id.root_overflow) ImageButton overflow;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        projectName = getIntent().getStringExtra("project");
        projectDir = new File(Constants.HYPER_ROOT + File.separator + projectName);
        indexFile = ProjectManager.getIndexFile(projectName);

        setTheme(Styles.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("files")) {
            openFiles = getIntent().getStringArrayListExtra("files");
        } else {
            openFiles = new ArrayList<>();
            openFiles.add(indexFile.getPath());
        }

        props = HTMLParser.getProperties(projectName);
        fileSpinner = new Spinner(this);
        fileAdapter = new FileAdapter(this, openFiles);
        fileSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        fileSpinner.setAdapter(fileAdapter);
        toolbar.addView(fileSpinner);
        if (Prefs.get(this, "dark_theme", false)) {
            toolbar.setPopupTheme(R.style.Hyper_Dark);
        }

        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_fragment, getFragment(openFiles.get(position)))
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

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                props = HTMLParser.getProperties(projectName);
                headerTitle.setText(props[0]);
                headerDesc.setText(props[1]);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        rootNode = TreeNode.root();
        setupFileTree(rootNode, projectDir);
        treeView = new AndroidTreeView(ProjectActivity.this, rootNode);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultViewHolder(FileTreeHolder.class);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle);
        treeView.setDefaultNodeClickListener(new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {
                FileTreeHolder.FileTreeItem item = (FileTreeHolder.FileTreeItem) value;
                if (node.isLeaf() && item.file.isFile()) {
                    if (openFiles.contains(item.file.getPath())) {
                        setFragment(item.file.getPath(), false);
                        drawerLayout.closeDrawers();
                    } else {
                        if (!ProjectManager.isBinaryFile(item.file)) {
                            setFragment(item.file.getPath(), true);
                            drawerLayout.closeDrawers();
                        } else if (ProjectManager.isImageFile(item.file)) {
                            setFragment(item.file.getPath(), true);
                            drawerLayout.closeDrawers();
                        } else {
                            Snackbar.make(drawerLayout, R.string.not_text_file, Snackbar.LENGTH_SHORT).show();
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
                                        drawerLayout,
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

                                snackbar.addCallback(new Snackbar.Callback() {
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
        headerBackground.setBackgroundResource(MATERIAL_BACKGROUNDS[(int) (Math.random() * 8)]);
        headerIcon.setImageBitmap(ProjectManager.getFavicon(ProjectActivity.this, projectName));
        headerTitle.setText(props[0]);
        headerDesc.setText(props[1]);

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(ProjectActivity.this, overflow);
                menu.getMenuInflater().inflate(R.menu.menu_file_options, menu.getMenu());
                menu.getMenu().findItem(R.id.action_copy).setVisible(false);
                menu.getMenu().findItem(R.id.action_cut).setVisible(false);
                menu.getMenu().findItem(R.id.action_rename).setVisible(false);
                menu.getMenu().findItem(R.id.action_paste).setEnabled(Clipboard.getInstance().getCurrentFile() != null);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_new_file:
                                AlertDialog.Builder newFileBuilder = new AlertDialog.Builder(ProjectActivity.this);
                                View newFileRootView = LayoutInflater.from(ProjectActivity.this).inflate(R.layout.dialog_input_single, null, false);
                                final TextInputEditText fileName = newFileRootView.findViewById(R.id.input_text);
                                fileName.setHint(R.string.file_name);

                                newFileBuilder.setTitle("New file");
                                newFileBuilder.setView(newFileRootView);
                                newFileBuilder.setPositiveButton(R.string.create, null);
                                newFileBuilder.setNegativeButton(R.string.cancel, null);

                                final AlertDialog newFileDialog = newFileBuilder.create();
                                newFileDialog.show();
                                newFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (fileName.getText().toString().isEmpty()) {
                                            fileName.setError("Please enter a file name");
                                        } else {
                                            newFileDialog.dismiss();
                                            String fileStr = fileName.getText().toString();
                                            File newFile = new File(projectDir, fileStr);
                                            try {
                                                FileUtils.writeStringToFile(newFile, "\n", Charset.defaultCharset());
                                            } catch (IOException e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }

                                            Snackbar.make(drawerLayout, "Created " + fileStr + ".", Snackbar.LENGTH_SHORT).show();
                                            TreeNode newFileNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(newFile), newFile, drawerLayout));
                                            rootNode.addChild(newFileNode);
                                            treeView.setRoot(rootNode);
                                            treeView.addNode(rootNode, newFileNode);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_new_folder:
                                AlertDialog.Builder newFolderBuilder = new AlertDialog.Builder(ProjectActivity.this);
                                View newFolderRootView = LayoutInflater.from(ProjectActivity.this).inflate(R.layout.dialog_input_single, null, false);
                                final TextInputEditText folderName = newFolderRootView.findViewById(R.id.input_text);
                                folderName.setHint(R.string.folder_name);

                                newFolderBuilder.setTitle("New folder");
                                newFolderBuilder.setView(newFolderRootView);
                                newFolderBuilder.setPositiveButton(R.string.create, null);
                                newFolderBuilder.setNegativeButton(R.string.cancel, null);

                                final AlertDialog newFolderDialog = newFolderBuilder.create();
                                newFolderDialog.show();
                                newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (folderName.getText().toString().isEmpty()) {
                                            folderName.setError("Please enter a folder name");
                                        } else {
                                            newFolderDialog.dismiss();
                                            String folderStr = folderName.getText().toString();
                                            File newFolder = new File(projectDir, folderStr);
                                            try {
                                                FileUtils.forceMkdir(newFolder);
                                            } catch (IOException e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }

                                            Snackbar.make(drawerLayout, "Created " + folderStr + ".", Snackbar.LENGTH_SHORT).show();
                                            TreeNode newFolderNode = new TreeNode(new FileTreeHolder.FileTreeItem(R.drawable.ic_folder, newFolder, drawerLayout));
                                            rootNode.addChild(newFolderNode);
                                            treeView.setRoot(rootNode);
                                            treeView.addNode(rootNode, newFolderNode);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_paste:
                                File currentFile = Clipboard.getInstance().getCurrentFile();
                                TreeNode currentNode = Clipboard.getInstance().getCurrentNode();
                                FileTreeHolder.FileTreeItem currentItem = (FileTreeHolder.FileTreeItem) currentNode.getValue();
                                switch (Clipboard.getInstance().getType()) {
                                    case COPY:
                                        if (currentFile.isDirectory()) {
                                            try {
                                                FileUtils.copyDirectoryToDirectory(currentFile, projectDir);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                FileUtils.copyFileToDirectory(currentFile, projectDir);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }

                                        Snackbar.make(drawerLayout, "Successfully copied " + currentFile.getName() + ".", Snackbar.LENGTH_SHORT).show();
                                        File copyFile = new File(projectDir, currentFile.getName());
                                        TreeNode copyNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(copyFile), copyFile, currentItem.view));
                                        rootNode.addChild(copyNode);
                                        treeView.setRoot(rootNode);
                                        treeView.addNode(rootNode, copyNode);
                                        break;
                                    case CUT:
                                        if (currentFile.isDirectory()) {
                                            try {
                                                FileUtils.moveDirectoryToDirectory(currentFile, projectDir, false);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                FileUtils.moveFileToDirectory(currentFile, projectDir, false);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }

                                        Snackbar.make(drawerLayout, "Successfully moved " + currentFile.getName() + ".", Snackbar.LENGTH_SHORT).show();
                                        Clipboard.getInstance().setCurrentFile(null);
                                        File cutFile = new File(projectDir, currentFile.getName());
                                        TreeNode cutNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(cutFile), cutFile, currentItem.view));
                                        rootNode.addChild(cutNode);
                                        treeView.setRoot(rootNode);
                                        treeView.addNode(rootNode, cutNode);
                                        treeView.removeNode(Clipboard.getInstance().getCurrentNode());
                                        break;
                                }
                                return true;
                        }

                        return false;
                    }
                });

                menu.show();
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(projectName, ProjectManager.getFavicon(ProjectActivity.this, projectName));
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
                TreeNode folderNode = new TreeNode(new FileTreeHolder.FileTreeItem(R.drawable.ic_folder, file, drawerLayout));
                setupFileTree(folderNode, file);
                root.addChild(folderNode);
            } else {
                TreeNode fileNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(file), file, drawerLayout));
                root.addChild(fileNode);
            }
        }
    }

    private void removeFragment(String file) {
        openFiles.remove(file);
        fileAdapter.remove(file);
        fileAdapter.notifyDataSetChanged();
    }

    /**
     * Open file when selected by setting the correct fragment
     *
     * @param file file to open
     * @param add whether to add to adapter
     */
    private void setFragment(String file, boolean add) {
        if (add) {
            fileAdapter.add(file);
            fileAdapter.notifyDataSetChanged();
        }

        fileSpinner.setSelection(fileAdapter.getPosition(file), true);
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
        bundle.putInt("position", fileAdapter.getCount());
        bundle.putString("location", title);
        if (ProjectManager.isImageFile(new File(title))) {
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
        boolean isGitRepo = new File(projectDir, ".git").exists() && new File(projectDir, ".git").isDirectory();
        boolean canCommit = false;
        boolean canCheckout = false;
        boolean hasRemotes = false;
        boolean isHtml = ((String) fileSpinner.getSelectedItem()).endsWith(".html");
        if (isGitRepo) {
            canCommit = GitWrapper.canCommit(drawerLayout, projectDir);
            canCheckout = GitWrapper.canCheckout(drawerLayout, projectDir);
            hasRemotes = GitWrapper.getRemotes(drawerLayout, projectDir) != null &&
                    GitWrapper.getRemotes(drawerLayout, projectDir).size() > 0;
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
        toggle.syncState();
    }

    /**
     * Called when config is changed
     *
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
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
                runIntent.putExtra("url", "file:///" + indexFile.getPath());
                runIntent.putExtra("name", projectName);
                startActivity(runIntent);
                return true;
            case R.id.action_view:
                Intent viewIntent = new Intent(ProjectActivity.this, ViewActivity.class);
                viewIntent.putExtra("html_path", openFiles.get(fileSpinner.getSelectedItemPosition()));
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
                GitWrapper.init(ProjectActivity.this, projectDir, drawerLayout);
                return true;
            case R.id.action_git_add:
                GitWrapper.add(drawerLayout, projectDir);
                return true;
            case R.id.action_git_commit:
                AlertDialog.Builder gitCommitBuilder = new AlertDialog.Builder(ProjectActivity.this);
                View view = LayoutInflater.from(ProjectActivity.this).inflate(R.layout.dialog_input_single, null, false);
                final TextInputEditText editText = view.findViewById(R.id.input_text);
                gitCommitBuilder.setTitle(R.string.git_commit);
                editText.setHint(R.string.commit_message);
                gitCommitBuilder.setView(view);
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
                        if (!editText.getText().toString().isEmpty()) {
                            GitWrapper.commit(ProjectActivity.this, drawerLayout, projectDir, editText.getText().toString());
                            dialog4.dismiss();
                        } else {
                            editText.setError(getString(R.string.commit_message_empty));
                        }
                    }
                });
                return true;
            case R.id.action_git_push:
                AlertDialog.Builder gitPushBuilder = new AlertDialog.Builder(ProjectActivity.this);
                gitPushBuilder.setTitle("Push changes");

                View pushView = LayoutInflater.from(ProjectActivity.this)
                        .inflate(R.layout.dialog_push, null, false);

                final Spinner spinner = pushView.findViewById(R.id.remotes_spinner);
                final CheckBox dryRun = pushView.findViewById(R.id.dry_run);
                final CheckBox force = pushView.findViewById(R.id.force);
                final CheckBox thin = pushView.findViewById(R.id.thin);
                final CheckBox tags = pushView.findViewById(R.id.tags);

                final TextInputEditText pushUsername = pushView.findViewById(R.id.push_username);
                final TextInputEditText pushPassword = pushView.findViewById(R.id.push_password);

                spinner.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, GitWrapper.getRemotes(drawerLayout, projectDir)));
                gitPushBuilder.setView(pushView);
                gitPushBuilder.setPositiveButton("PUSH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        GitWrapper.push(ProjectActivity.this, drawerLayout, projectDir, (String) spinner.getSelectedItem(), new boolean[]{dryRun.isChecked(), force.isChecked(), thin.isChecked(), tags.isChecked()}, pushUsername.getText().toString(), pushPassword.getText().toString());
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

                final Spinner spinner1 = pullView.findViewById(R.id.remotes_spinner);

                final TextInputEditText pullUsername = pullView.findViewById(R.id.pull_username);
                final TextInputEditText pullPassword = pullView.findViewById(R.id.pull_password);

                spinner1.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, GitWrapper.getRemotes(drawerLayout, projectDir)));
                gitPullBuilder.setView(pullView);
                gitPullBuilder.setPositiveButton("PULL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        GitWrapper.pull(ProjectActivity.this, drawerLayout, projectDir, (String) spinner1.getSelectedItem(), pullUsername.getText().toString(), pullPassword.getText().toString());
                    }
                });

                gitPullBuilder.setNegativeButton(R.string.cancel, null);
                gitPullBuilder.create().show();
            case R.id.action_git_log:
                List<RevCommit> commits = GitWrapper.getCommits(drawerLayout, projectDir);
                @SuppressLint("InflateParams") View layoutLog = inflater.inflate(R.layout.sheet_logs, null);
                if (Prefs.get(this, "dark_theme", false)) {
                    layoutLog.setBackgroundColor(0xFF333333);
                }

                RecyclerView logsList = layoutLog.findViewById(R.id.logs_list);
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
                final List<RevCommit> commitsToDiff = GitWrapper.getCommits(drawerLayout, projectDir);
                assert commitsToDiff != null;
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
                                SpannableString string = GitWrapper.diff(drawerLayout, projectDir, commitsToDiff.get(chosen[0]).getId(), commitsToDiff.get(chosen[1]).getId());
                                View rootView = inflater.inflate(R.layout.dialog_diff, null, false);
                                DiffView diffView = rootView.findViewById(R.id.diff_view);
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
                if (Prefs.get(this, "dark_theme", false)) {
                    layoutStatus.setBackgroundColor(0xFF333333);
                }

                TextView conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders;
                conflict = layoutStatus.findViewById(R.id.status_conflicting);
                added = layoutStatus.findViewById(R.id.status_added);
                changed = layoutStatus.findViewById(R.id.status_changed);
                missing = layoutStatus.findViewById(R.id.status_missing);
                modified = layoutStatus.findViewById(R.id.status_modified);
                removed = layoutStatus.findViewById(R.id.status_removed);
                uncommitted = layoutStatus.findViewById(R.id.status_uncommitted);
                untracked = layoutStatus.findViewById(R.id.status_untracked);
                untrackedFolders = layoutStatus.findViewById(R.id.status_untracked_folders);

                GitWrapper.status(drawerLayout, projectDir, conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders);

                BottomSheetDialog dialogStatus = new BottomSheetDialog(this);
                dialogStatus.setContentView(layoutStatus);
                dialogStatus.show();
                return true;
            case R.id.action_git_branch_new:
                AlertDialog.Builder gitBranch = new AlertDialog.Builder(ProjectActivity.this);
                View branchView = LayoutInflater.from(ProjectActivity.this).inflate(R.layout.dialog_git_branch, null, false);
                gitBranch.setTitle("New branch");
                final EditText editText5 = branchView.findViewById(R.id.branch_name);
                final CheckBox checkBox = branchView.findViewById(R.id.checkout);
                checkBox.setText(R.string.checkout);
                gitBranch.setView(branchView);
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
                            GitWrapper.createBranch(ProjectActivity.this, drawerLayout, projectDir, editText5.getText().toString(), checkBox.isChecked());
                            dialog5.dismiss();
                        } else {
                            editText5.setError(getString(R.string.branch_name_empty));
                        }
                    }
                });
                return true;
            case R.id.action_git_branch_remove:
                AlertDialog.Builder gitRemove = new AlertDialog.Builder(this);
                final List<Ref> branchesList = GitWrapper.getBranches(drawerLayout, projectDir);
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
                            GitWrapper.deleteBranch(drawerLayout, projectDir, toDelete.toArray(new String[toDelete.size()]));
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
                final List<Ref> branches = GitWrapper.getBranches(drawerLayout, projectDir);
                int checkedItem = -1;
                CharSequence[] items = new CharSequence[0];
                if (branches != null) {
                    items = new CharSequence[branches.size()];
                    for (int i = 0; i < items.length; i++) {
                        items[i] = branches.get(i).getName();
                    }
                }

                for (int i = 0; i < items.length; i++) {
                    String branch = GitWrapper.getCurrentBranch(drawerLayout, projectDir);
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
                        GitWrapper.checkout(ProjectActivity.this, drawerLayout, projectDir, branches.get(i).getName());
                    }
                });

                gitCheckout.setNegativeButton(R.string.close, null);
                gitCheckout.setTitle("Checkout branch");
                gitCheckout.create().show();
                return true;
            case R.id.action_git_remote:
                Intent remoteIntent = new Intent(ProjectActivity.this, RemotesActivity.class);
                remoteIntent.putExtra("project_file", projectDir.getPath());
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
                    final TextInputEditText editText = view.findViewById(R.id.input_text);
                    editText.setHint(R.string.file_name);
                    builder.setView(view);
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
                                if (ProjectManager.importFile(ProjectActivity.this, projectName, fileUri, editText.getText().toString())) {
                                    Snackbar.make(drawerLayout, R.string.file_success, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(drawerLayout, R.string.file_fail, Snackbar.LENGTH_LONG).show();
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
                    intent.putStringArrayListExtra("files", openFiles);
                    startActivity(intent);
                    finish();
                }

                break;
        }

        setupFileTree(rootNode, projectDir);
        treeView.setRoot(rootNode);
    }

    /**
     * Method to show about dialog holding project information
     */
    @SuppressLint("InflateParams")
    private void showAbout() {
        props = HTMLParser.getProperties(projectName);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.sheet_about, null);

        TextView name = layout.findViewById(R.id.project_name);
        TextView author = layout.findViewById(R.id.project_author);
        TextView description = layout.findViewById(R.id.project_description);
        TextView keywords = layout.findViewById(R.id.project_keywords);

        name.setText(props[0]);
        author.setText(props[1]);
        description.setText(props[2]);
        keywords.setText(props[3]);

        if (Prefs.get(this, "dark_theme", false)) {
            layout.setBackgroundColor(0xFF333333);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(layout);
        dialog.show();
    }
}
