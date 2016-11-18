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

package io.geeteshk.hyper.wysiwyg;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.text.HtmlCompat;

public class TagTreeHolder extends TreeNode.BaseNodeViewHolder<TagTreeHolder.TagTreeItem> {

    ImageView arrow;

    public TagTreeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, TagTreeItem value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_element, null, false);
        TextView tagName = (TextView) view.findViewById(R.id.element_name);
        arrow = (ImageView) view.findViewById(R.id.element_arrow);

        Spanned element = HtmlCompat.fromHtml("\t&lt;<font color=\"#f92672\">" + value.element.tagName() + "</font>&gt;");
        tagName.setText(element);

        if (node.isLeaf()) {
            arrow.setVisibility(View.GONE);
        }

        if (!node.isExpanded()) {
            arrow.setRotation(-90);
        }

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
