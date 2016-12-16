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

package io.geeteshk.hyper.widget.holder;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import org.jsoup.nodes.Element;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.ProjectActivity;
import io.geeteshk.hyper.adapter.AttrsAdapter;
import io.geeteshk.hyper.text.HtmlCompat;

public class TagTreeHolder extends TreeNode.BaseNodeViewHolder<TagTreeHolder.TagTreeItem> {

    ImageView arrow;

    public TagTreeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final TagTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_element, null, false);
        TextView tagName = (TextView) view.findViewById(R.id.element_name);
        arrow = (ImageView) view.findViewById(R.id.element_arrow);
        final ImageButton overflow = (ImageButton) view.findViewById(R.id.element_overflow);

        Spanned element = HtmlCompat.fromHtml("\t&lt;<font color=\"#f92672\">" + value.element.tagName() + "</font>&gt;");
        tagName.setText(element);

        if (node.isLeaf()) {
            arrow.setVisibility(View.GONE);
        }

        if (!node.isExpanded()) {
            arrow.setRotation(-90);
        }

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu menu = new PopupMenu(context, overflow);
                menu.getMenuInflater().inflate(R.menu.menu_tag_options, menu.getMenu());
                if (value.element.tagName().equals("head") || value.element.tagName().equals("body")) {
                    menu.getMenu().findItem(R.id.action_tag_remove).setVisible(false);
                }

                if (!value.element.isBlock()) {
                    menu.getMenu().findItem(R.id.action_tag_add).setVisible(false);
                }

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_tag_add:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Add to " + value.element.tagName());
                                View rootView = inflater.inflate(R.layout.dialog_element_add, null, false);
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
                                return true;
                            case R.id.action_tag_edit:
                                final Element element = value.element;
                                View rootView2 = inflater.inflate(R.layout.dialog_element_info, null, false);
                                RecyclerView elementAttrs = (RecyclerView) rootView2.findViewById(R.id.element_attrs);
                                AttrsAdapter adapter = new AttrsAdapter(context, element);
                                LinearLayoutManager manager = new LinearLayoutManager(context);
                                final TextView elementTag = (TextView) rootView2.findViewById(R.id.element_tag);
                                final TextView elementText = (TextView) rootView2.findViewById(R.id.element_text);
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);

                                elementAttrs.addItemDecoration(new DividerItemDecoration(elementAttrs.getContext(), manager.getOrientation()));
                                elementAttrs.setLayoutManager(manager);
                                elementAttrs.setHasFixedSize(true);
                                elementAttrs.setAdapter(adapter);
                                elementTag.setText(element.tagName());
                                elementTag.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        View viewElement = inflater.inflate(R.layout.dialog_input_single, null, false);
                                        final TextInputEditText editText = (TextInputEditText) viewElement.findViewById(R.id.input_text);
                                        builder.setTitle("Change element tag");
                                        editText.setHint("Value");
                                        editText.setSingleLine(true);
                                        editText.setMaxLines(1);
                                        editText.setText(element.tagName());
                                        builder.setView(viewElement);
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
                                        View viewElement = inflater.inflate(R.layout.dialog_input_single, null, false);
                                        final TextInputEditText editText = (TextInputEditText) viewElement.findViewById(R.id.input_text);
                                        builder.setTitle("Change element text");
                                        editText.setHint("Value");
                                        editText.setSingleLine(true);
                                        editText.setMaxLines(1);
                                        editText.setText(element.ownText());
                                        builder.setView(viewElement);
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

                                builder2.setView(rootView2);
                                builder2.create().show();
                                return true;
                            case R.id.action_tag_remove:
                                tView.removeNode(node);
                                value.element.remove();
                                return true;
                        }

                        return true;
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

    public static class TagTreeItem {

        public Element element;

        public TagTreeItem(Element element) {
            this.element = element;
        }
    }
}
