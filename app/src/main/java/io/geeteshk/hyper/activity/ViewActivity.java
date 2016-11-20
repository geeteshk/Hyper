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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.AttrsAdapter;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.widget.TagTreeHolder;

public class ViewActivity extends AppCompatActivity {

    Document document;
    String mProject, mFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theme.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        mProject = getIntent().getStringExtra("project");
        String htmlPath = getIntent().getStringExtra("html_path");
        File html = new File(htmlPath);
        mFilename = html.getName();
        TreeNode rootNode = TreeNode.root();
        try {
            setupViewTree(rootNode, html);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout viewLayout = (LinearLayout) findViewById(R.id.view_layout);
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
                Project.createFile(mProject, mFilename, document.outerHtml());
                setResult(RESULT_OK);
                Toast.makeText(ViewActivity.this, "Saved file.", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewTree(TreeNode root, File html) throws IOException {
        document = Jsoup.parse(html, "UTF-8");

        Element head = document.head();
        TreeNode headNode = new TreeNode(new TagTreeHolder.TagTreeItem(head));
        setupElementTree(headNode, head);
        root.addChild(headNode);

        Element body = document.body();
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
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Project.createFile(mProject, mFilename, document.outerHtml());
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
