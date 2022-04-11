package net.witixin.mountables2.data.files;

public enum ResourceType {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    ASSETS("assets"),
    DATA("data");

    private final String folderName;

    private ResourceType(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    @Override
    public String toString() {
        return getFolderName();
    }
}
