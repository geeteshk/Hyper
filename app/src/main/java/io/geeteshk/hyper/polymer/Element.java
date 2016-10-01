package io.geeteshk.hyper.polymer;

/**
 * Polymer Element class
 */
public class Element {

    /**
     * Element info
     */
    private String name;
    private String description;
    private String prefix;
    private String version;

    /**
     * public Constructor
     *
     * @param name name
     * @param description description
     * @param prefix prefix
     * @param version version
     */
    public Element(String name, String description, String prefix, String version) {
        this.name = name;
        this.description = description;
        this.prefix = prefix;
        this.version = version;
    }

    /**
     * Get name of element
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get description of element
     *
     * @return name
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get prefix of element
     *
     * @return name
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get version of element
     *
     * @return name
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Used for comparing elements
     *
     * @param obj object to compare
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Element)) {
            return false;
        }

        Element other = (Element) obj;
        return name.equals(other.getName());
    }

    /**
     * Get hashcode of element
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
