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

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.AttrsAdapter;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.text.HtmlCompat;
import io.geeteshk.hyper.widget.FileTreeHolder;
import io.geeteshk.hyper.wysiwyg.TagTreeHolder;

public class ViewActivity extends AppCompatActivity {

    Document document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theme.getThemeInt(this));
        super.onCreate(savedInstanceState);
        String htmlPath = getIntent().getStringExtra("html_path");
        File html = new File(htmlPath);
        TreeNode rootNode = TreeNode.root();
        try {
            setupViewTree(rootNode, html);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AndroidTreeView treeView = new AndroidTreeView(ViewActivity.this, rootNode);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultViewHolder(TagTreeHolder.class);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle);
        treeView.setDefaultNodeLongClickListener(new TreeNode.TreeNodeLongClickListener() {
            @Override
            public boolean onLongClick(TreeNode node, Object value) {
                Element element = ((TagTreeHolder.TagTreeItem) value).element;
                ArrayList<Attribute> attributes = new ArrayList<>();
                for (Attribute attribute : element.attributes()) {
                    attributes.add(attribute);
                }

                View rootView = LayoutInflater.from(ViewActivity.this).inflate(R.layout.dialog_element_info, null, false);
                RecyclerView elementAttrs = (RecyclerView) rootView.findViewById(R.id.element_attrs);
                AttrsAdapter adapter = new AttrsAdapter(attributes);
                LinearLayoutManager manager = new LinearLayoutManager(ViewActivity.this);
                TextView elementTag = (TextView) rootView.findViewById(R.id.element_tag);
                TextView elementText = (TextView) rootView.findViewById(R.id.element_text);
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.this);

                elementAttrs.setLayoutManager(manager);
                elementAttrs.setHasFixedSize(true);
                elementAttrs.setAdapter(adapter);
                elementTag.setText(element.tagName());

                if (!element.ownText().isEmpty()) {
                    elementText.setText(element.ownText());
                } else {
                    elementText.setText("...");
                }

                builder.setView(rootView);
                builder.create().show();
                return true;
            }
        });

        setContentView(treeView.getView());
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
}
