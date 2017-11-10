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
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Styles;
import io.geeteshk.hyper.widget.holder.TagTreeHolder;

public class ViewActivity extends AppCompatActivity {

    private static final String TAG = ViewActivity.class.getSimpleName();

    Document htmlDoc;
    File htmlFile;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.view_layout) LinearLayout viewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Styles.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        ButterKnife.bind(this);

        String htmlPath = getIntent().getStringExtra("html_path");
        htmlFile = new File(htmlPath);
        TreeNode rootNode = TreeNode.root();
        try {
            setupViewTree(rootNode, htmlFile);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        setSupportActionBar(toolbar);
        toolbar.setTitle(htmlFile.getName());
        toolbar.setSubtitle(htmlFile.getPath().substring(htmlFile.getPath().indexOf("Hyper/") + 6));

        AndroidTreeView treeView = new AndroidTreeView(ViewActivity.this, rootNode);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultViewHolder(TagTreeHolder.class);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle);
        viewLayout.addView(treeView.getView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                try {
                    FileUtils.writeStringToFile(htmlFile, htmlDoc.outerHtml(), Charset.defaultCharset(), false);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                setResult(RESULT_OK);
                Snackbar.make(viewLayout, R.string.save_changes_done, Snackbar.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewTree(TreeNode root, File html) throws IOException {
        htmlDoc = Jsoup.parse(html, "UTF-8");

        Element head = htmlDoc.head();
        TreeNode headNode = new TreeNode(new TagTreeHolder.TagTreeItem(head));
        setupElementTree(headNode, head);
        root.addChild(headNode);

        Element body = htmlDoc.body();
        TreeNode bodyNode = new TreeNode(new TagTreeHolder.TagTreeItem(body));
        setupElementTree(bodyNode, body);
        root.addChild(bodyNode);
    }

    private void setupElementTree(TreeNode root, Element element) {
        Elements children = element.children();
        for (Element child : children) {
            TreeNode elementNode = new TreeNode(new TagTreeHolder.TagTreeItem(child));
            setupElementTree(elementNode, child);
            root.addChild(elementNode);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.this);
        builder.setTitle("Save changes?");
        builder.setMessage("This will append any changes to the html file. If you choose to discard unsaved changes will not be saved.");
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    FileUtils.writeStringToFile(htmlFile, htmlDoc.outerHtml(), Charset.defaultCharset(), false);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                setResult(RESULT_OK);
                finish();
            }
        });

        builder.setNegativeButton("DISCARD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.create().show();
    }
}
