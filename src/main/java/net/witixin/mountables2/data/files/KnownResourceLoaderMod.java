package net.witixin.mountables2.data.files;

import java.io.File;

public enum KnownResourceLoaderMod {
    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    THE_LOADER("theloader", "the_loader/resourcepacks/", "the_loader/datapacks/", "mountables2"),
    OPEN_LOADER("openloader", "openloader/resources/", "openloader/data/", "mountables2"),
    GLOBAL_DATA_AND_RESOURCE_PACKS("globaldataandresourcepacks", "global_resource_packs/", "global_data_packs/", "mountables2");

    final String modid;
    final File resourcePackDirectory;
    final File datapackDirectory;

    KnownResourceLoaderMod(String loadermodid, String resourcePackDirectory, String datapackDirectory, String customModid) {
        this.modid = loadermodid;
        this.resourcePackDirectory = new File(resourcePackDirectory + customModid);
        this.datapackDirectory = new File(datapackDirectory + customModid);
    }
}
