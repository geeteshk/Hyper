package io.geeteshk.hyper.helper;

import android.view.View;

import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

public class Clippy {

    private static final Clippy clippy = new Clippy();
    private File currentFile = null;
    private Type type = Type.COPY;
    private TreeNode currentNode = null;

    public enum Type {
        COPY, CUT
    }

    public static Clippy getInstance() {
        return clippy;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public TreeNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(TreeNode currentNode) {
        this.currentNode = currentNode;
    }
}
