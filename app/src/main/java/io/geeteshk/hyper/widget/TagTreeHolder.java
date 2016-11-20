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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;

import org.jsoup.nodes.Element;

import java.io.File;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.ProjectActivity;
import io.geeteshk.hyper.activity.ViewActivity;
import io.geeteshk.hyper.adapter.AttrsAdapter;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.text.HtmlCompat;

public class TagTreeHolder extends TreeNode.BaseNodeViewHolder<TagTreeHolder.TagTreeItem> {

    ImageView arrow;

    public TagTreeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final TagTreeItem value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_element, null, false);
        TextView tagName = (TextView) view.findViewById(R.id.element_name);
        ImageButton tagAdd = (ImageButton) view.findViewById(R.id.element_add);
        ImageButton tagEdit = (ImageButton) view.findViewById(R.id.element_edit);
        ImageButton tagDelete = (ImageButton) view.findViewById(R.id.element_delete);
        arrow = (ImageView) view.findViewById(R.id.element_arrow);

        Spanned element = HtmlCompat.fromHtml("\t&lt;<font color=\"#f92672\">" + value.element.tagName() + "</font>&gt;");
        tagName.setText(element);

        if (node.isLeaf()) {
            arrow.setVisibility(View.GONE);
        }

        if (!node.isExpanded()) {
            arrow.setRotation(-90);
        }

        if (value.element.tagName().equals("head") || value.element.tagName().equals("body")) {
            tagDelete.setVisibility(View.GONE);
        }

        if (!value.element.isBlock()) {
            tagAdd.setVisibility(View.GONE);
        }

        tagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Add to " + value.element.tagName());
                View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_element_add, null, false);
                final TextInputEditText nameText = (TextInputEditText) rootView.findViewById(R.id.element_name_text);
                final TextInputEditText textText = (TextInputEditText) rootView.findViewById(R.id.element_text_text);
                builder.setView(rootView);
                builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setNegativeButton("CANCEL", null);
                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!nameText.getText().toString().isEmpty()) {
                            tView.addNode(node, new TreeNode(new TagTreeItem(value.element.appendElement(nameText.getText().toString()).text(textText.getText().toString()))));
                            dialog.dismiss();
                        } else {
                            nameText.setError("Please enter a name for the element.");
                        }
                    }
                });
            }
        });
        
        tagEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Element element = value.element;
                View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_element_info, null, false);
                RecyclerView elementAttrs = (RecyclerView) rootView.findViewById(R.id.element_attrs);
                AttrsAdapter adapter = new AttrsAdapter(context, element);
                LinearLayoutManager manager = new LinearLayoutManager(context);
                final TextView elementTag = (TextView) rootView.findViewById(R.id.element_tag);
                final TextView elementText = (TextView) rootView.findViewById(R.id.element_text);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                elementAttrs.addItemDecoration(new DividerItemDecoration(elementAttrs.getContext(), manager.getOrientation()));
                elementAttrs.setLayoutManager(manager);
                elementAttrs.setHasFixedSize(true);
                elementAttrs.setAdapter(adapter);
                elementTag.setText(element.tagName());
                elementTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Change element tag");
                        final EditText editText = new EditText(context);
                        editText.setHint("Value");
                        editText.setSingleLine(true);
                        editText.setMaxLines(1);
                        editText.setText(element.tagName());
                        builder.setView(editText);
                        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        builder.setNegativeButton("CANCEL", null);
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!editText.getText().toString().isEmpty()) {
                                    element.tagName(editText.getText().toString());
                                    elementTag.setText(editText.getText().toString());
                                    TreeNode parent = node.getParent();
                                    tView.removeNode(node);
                                    tView.addNode(parent, new TreeNode(new TagTreeItem(value.element)));
                                    dialog.dismiss();
                                } else {
                                    editText.setError("Please enter a name for the element.");
                                }
                            }
                        });
                    }
                });

                elementText.setText(element.ownText());
                elementText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Change element tag");
                        final EditText editText = new EditText(context);
                        editText.setHint("Value");
                        editText.setSingleLine(true);
                        editText.setMaxLines(1);
                        editText.setText(element.ownText());
                        builder.setView(editText);
                        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                element.text(editText.getText().toString());
                                elementText.setText(editText.getText().toString());
                            }
                        });

                        builder.setNegativeButton("CANCEL", null);
                        builder.create().show();
                    }
                });

                builder.setView(rootView);
                builder.create().show();
            }
        });

        tagDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tView.removeNode(node);
                value.element.remove();
            }
        });

        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrow.animate().rotation(active ? 0 : -90);
    }

    public static class TagTreeItem {

        public Element element;

        public TagTreeItem(Element element) {
            this.element = element;
        }
    }
}
