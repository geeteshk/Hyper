package io.geeteshk.hyper.polymer;

import java.util.ArrayList;

/**
 * Holder instance class for catalog
 */
public class ElementsHolder {

    /**
     * Holder instance
     */
    private static final ElementsHolder holder = new ElementsHolder();

    /**
     * ELements
     */
    private ArrayList<Element> mElements = new ArrayList<>();

    /**
     * Project
     */
    private String mProject;

    /**
     * Get holder instance
     *
     * @return holder
     */
    public static ElementsHolder getInstance() {
        return holder;
    }

    /**
     * Get elements
     *
     * @return elements
     */
    public ArrayList<Element> getElements() {
        return mElements;
    }

    /**
     * Set elements
     *
     * @param elements to set
     */
    public void setElements(ArrayList<Element> elements) {
        mElements = elements;
    }

    /**
     * Get project
     *
     * @return project
     */
    public String getProject() {
        return mProject;
    }

    /**
     * Set project
     *
     * @param project to set
     */
    public void setProject(String project) {
        mProject = project;
    }
}
