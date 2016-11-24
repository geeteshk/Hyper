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

package io.geeteshk.hyper.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.ProjectActivity;
import io.geeteshk.hyper.helper.Clippy;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Project;

public class FileTreeHolder extends TreeNode.BaseNodeViewHolder<FileTreeHolder.FileTreeItem> {

    private static final String TAG = FileTreeHolder.class.getSimpleName();

    private ImageView arrow;

    public FileTreeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final FileTreeItem value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_file_browser, null, false);
        final TextView nodeFile = (TextView) view.findViewById(R.id.file_browser_name);
        final ImageView fileIcon = (ImageView) view.findViewById(R.id.file_browser_icon);
        arrow = (ImageView) view.findViewById(R.id.file_browser_arrow);
        final ImageButton overflow = (ImageButton) view.findViewById(R.id.file_browser_options);

        nodeFile.setText(value.text);
        fileIcon.setImageResource(value.icon);

        if (node.isLeaf()) {
            arrow.setVisibility(View.GONE);
        }

        if (!node.isExpanded()) {
            arrow.setRotation(-90);
        }

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final File file = new File(Constants.HYPER_ROOT + File.separator + value.project + File.separator + value.path);
                final PopupMenu menu = new PopupMenu(context, overflow);
                menu.getMenuInflater().inflate(R.menu.menu_file_options, menu.getMenu());
                if (file.isFile()) {
                    menu.getMenu().findItem(R.id.action_new).setVisible(false);
                    menu.getMenu().findItem(R.id.action_paste).setVisible(false);
                } else {
                    menu.getMenu().findItem(R.id.action_paste).setEnabled(Clippy.getInstance().getCurrentFile() != null);
                }

                if (file.getName().equals("index.html") && file.isFile()) {
                    menu.getMenu().findItem(R.id.action_cut).setVisible(false);
                    menu.getMenu().findItem(R.id.action_rename).setVisible(false);
                }

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_new_file:
                                AlertDialog.Builder newFileBuilder = new AlertDialog.Builder(context);
                                View newFileRootView = LayoutInflater.from(context).inflate(R.layout.dialog_input_single, null, false);
                                final TextInputEditText fileName = (TextInputEditText) newFileRootView.findViewById(R.id.input_text);
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
                                            File newFile = new File(file, fileStr);
                                            try {
                                                FileUtils.writeStringToFile(newFile, "\n", Charset.defaultCharset());
                                            } catch (IOException e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }

                                            Snackbar.make(value.view, "Created " + fileStr + ".", Snackbar.LENGTH_SHORT).show();
                                            String newFilePath = newFile.getPath().substring(newFile.getPath().indexOf(value.project + "/") + value.project.length(), newFile.getPath().length());
                                            TreeNode newFileNode = new TreeNode(new FileTreeItem(Decor.getIcon(newFile), fileStr, newFilePath, value.project, value.view));
                                            node.addChild(newFileNode);
                                            arrow.setVisibility(View.VISIBLE);
                                            tView.expandNode(node);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_new_folder:
                                AlertDialog.Builder newFolderBuilder = new AlertDialog.Builder(context);
                                View newFolderRootView = LayoutInflater.from(context).inflate(R.layout.dialog_input_single, null, false);
                                final TextInputEditText folderName = (TextInputEditText) newFolderRootView.findViewById(R.id.input_text);
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
                                            File newFolder = new File(file, folderStr);
                                            try {
                                                FileUtils.forceMkdir(newFolder);
                                            } catch (IOException e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }

                                            Snackbar.make(value.view, "Created " + folderStr + ".", Snackbar.LENGTH_SHORT).show();
                                            String newFolderPath = newFolder.getPath().substring(newFolder.getPath().indexOf(value.project + "/") + value.project.length(), newFolder.getPath().length());
                                            TreeNode newFolderNode = new TreeNode(new FileTreeItem(R.drawable.ic_folder, folderStr, newFolderPath, value.project, value.view));
                                            node.addChild(newFolderNode);
                                            arrow.setVisibility(View.VISIBLE);
                                            tView.expandNode(node);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_rename:
                                AlertDialog.Builder renameBuilder = new AlertDialog.Builder(context);
                                View renameRootView = LayoutInflater.from(context).inflate(R.layout.dialog_input_single, null, false);
                                final TextInputEditText renameTo = (TextInputEditText) renameRootView.findViewById(R.id.input_text);
                                renameTo.setHint(R.string.new_name);

                                renameBuilder.setTitle("Rename " + value.text);
                                renameBuilder.setView(renameRootView);
                                renameBuilder.setPositiveButton("RENAME", null);
                                renameBuilder.setNegativeButton(R.string.cancel, null);

                                final AlertDialog renameDialog = renameBuilder.create();
                                renameDialog.show();
                                renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (renameTo.getText().toString().isEmpty()) {
                                            renameTo.setError("Please enter a name");
                                        } else {
                                            renameDialog.dismiss();
                                            String renameStr = renameTo.getText().toString();
                                            File rename = new File(file.getPath().replace(file.getName(), renameStr));
                                            if (!file.isDirectory()) {
                                                try {
                                                    FileUtils.moveFile(file, rename);
                                                } catch (IOException e) {
                                                    Log.e(TAG, e.toString());
                                                    Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                try {
                                                    FileUtils.moveDirectory(file, rename);
                                                } catch (IOException e) {
                                                    Log.e(TAG, e.toString());
                                                    Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                                }
                                            }

                                            Snackbar.make(value.view, "Renamed " + value.text + " to " + renameStr + ".", Snackbar.LENGTH_SHORT).show();
                                            value.path = value.path.replace(value.text, renameStr);
                                            value.text = renameStr;
                                            value.icon = Decor.getIcon(rename);
                                            nodeFile.setText(value.text);
                                            fileIcon.setImageResource(value.icon);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_copy:
                                Clippy.getInstance().setCurrentFile(file);
                                Clippy.getInstance().setCurrentNode(node);
                                Clippy.getInstance().setType(Clippy.Type.COPY);
                                Snackbar.make(value.view, value.text + " selected to be copied.", Snackbar.LENGTH_SHORT).show();
                                return true;
                            case R.id.action_cut:
                                Clippy.getInstance().setCurrentFile(file);
                                Clippy.getInstance().setCurrentNode(node);
                                Clippy.getInstance().setType(Clippy.Type.CUT);
                                Snackbar.make(value.view, value.text + " selected to be moved.", Snackbar.LENGTH_SHORT).show();
                                return true;
                            case R.id.action_paste:
                                File currentFile = Clippy.getInstance().getCurrentFile();
                                TreeNode currentNode = Clippy.getInstance().getCurrentNode();
                                FileTreeItem currentItem = (FileTreeItem) currentNode.getValue();
                                switch (Clippy.getInstance().getType()) {
                                    case COPY:
                                        if (currentFile.isDirectory()) {
                                            try {
                                                FileUtils.copyDirectoryToDirectory(currentFile, file);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                FileUtils.copyFileToDirectory(currentFile, file);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }

                                        Snackbar.make(value.view, "Successfully copied " + currentFile.getName() + ".", Snackbar.LENGTH_SHORT).show();
                                        File copyFile = new File(file, currentFile.getName());
                                        String copyPath = copyFile.getPath().substring(copyFile.getPath().indexOf(value.project + "/") + value.project.length(), copyFile.getPath().length());
                                        TreeNode copyNode = new TreeNode(new FileTreeItem(Decor.getIcon(copyFile), currentItem.text, copyPath, currentItem.project, currentItem.view));
                                        node.addChild(copyNode);
                                        arrow.setVisibility(View.VISIBLE);
                                        tView.expandNode(node);
                                        break;
                                    case CUT:
                                        if (currentFile.isDirectory()) {
                                            try {
                                                FileUtils.moveDirectoryToDirectory(currentFile, file, false);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                FileUtils.moveFileToDirectory(currentFile, file, false);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(value.view, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }

                                        Snackbar.make(value.view, "Successfully moved " + currentFile.getName() + ".", Snackbar.LENGTH_SHORT).show();
                                        Clippy.getInstance().setCurrentFile(null);
                                        File cutFile = new File(file, currentFile.getName());
                                        String cutPath = cutFile.getPath().substring(cutFile.getPath().indexOf(value.project + "/") + value.project.length(), cutFile.getPath().length());
                                        TreeNode cutNode = new TreeNode(new FileTreeItem(Decor.getIcon(cutFile), currentItem.text, cutPath, currentItem.project, currentItem.view));
                                        node.addChild(cutNode);
                                        arrow.setVisibility(View.VISIBLE);
                                        tView.expandNode(node);
                                        tView.removeNode(Clippy.getInstance().getCurrentNode());
                                        break;
                                }

                                return true;
                            default:
                                return false;
                        }
                    }
                });

                menu.show();
            }
        });

        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrow.animate().rotation(active ? 0 : -90);
    }

    public static class FileTreeItem {

        @DrawableRes
        public int icon;

        public String text;
        public String path;
        public String project;

        public View view;

        public FileTreeItem(int icon, String text, String path, String project, View view) {
            this.icon = icon;
            this.text = text;
            this.path = path;
            this.project = project;
            this.view = view;
        }
    }
}
