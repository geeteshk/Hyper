package io.geeteshk.hyper.polymer;

import java.util.ArrayList;

public class ElementsHolder {

    private static final ElementsHolder holder = new ElementsHolder();
    private ArrayList<Element> mElements = new ArrayList<>();
    private String mProject;

    public static ElementsHolder getInstance() {
        return holder;
    }

    public ArrayList<Element> getElements() {
        return mElements;
    }

    public void setElements(ArrayList<Element> elements) {
        mElements = elements;
    }

    public String getProject() {
        return mProject;
    }

    public void setProject(String project) {
        mProject = project;
    }
}
