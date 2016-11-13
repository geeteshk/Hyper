package io.geeteshk.hyper.widget;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import io.geeteshk.hyper.R;

public class FileTreeHolder extends TreeNode.BaseNodeViewHolder<FileTreeHolder.FileTreeItem> {

    ImageView arrow;

    public FileTreeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, FileTreeItem value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_file_browser, null, false);
        TextView fileName = (TextView) view.findViewById(R.id.file_browser_name);
        ImageView fileIcon = (ImageView) view.findViewById(R.id.file_browser_icon);
        arrow = (ImageView) view.findViewById(R.id.file_browser_arrow);

        fileName.setText(value.text);
        fileIcon.setImageResource(value.icon);

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

    public static class FileTreeItem {

        @DrawableRes
        public int icon;

        public String text;
        public String path;

        public FileTreeItem(int icon, String text, String path) {
            this.icon = icon;
            this.text = text;
            this.path = path;
        }
    }
}
