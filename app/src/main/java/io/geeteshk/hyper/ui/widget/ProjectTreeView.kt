package io.geeteshk.hyper.ui.widget

import android.content.Context
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.widget.holder.FileTreeHolder

class ProjectTreeView(context: Context, rootNode: TreeNode) : AndroidTreeView(context, rootNode) {

    init {
        setDefaultAnimation(true)
        setDefaultViewHolder(FileTreeHolder::class.java)
        setDefaultContainerStyle(R.style.TreeNodeStyle)
    }
}