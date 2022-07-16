package net.witixin.mountables2.data.files;

import java.io.File;

public enum KnownResourceLoaderMod {
    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    //Currently not in 1.18
    //THE_LOADER("theloader", "the_loader/resourcepacks/", "the_loader/datapacks/", "mountables2"),

    //Seems broken in 1.18, doesn't reload on /reload)
    OPEN_LOADER("openloader", "config/openloader/resources/", "config/openloader/data/", "mountables2"),
    GLOBAL_DATA_AND_RESOURCE_PACKS("globaldataandresourcepacks", "global_packs/required_resources/", "global_packs/required_data/", "mountables2");

    final String modid;
    final File resourcePackDirectory;
    final File datapackDirectory;

    KnownResourceLoaderMod(String loadermodid, String resourcePackDirectory, String datapackDirectory, String customModid) {
        this.modid = loadermodid;
        this.resourcePackDirectory = new File(resourcePackDirectory  + "mountable_resourcepack/assets/" + customModid);
        this.datapackDirectory = new File(datapackDirectory   + "mountable_datapack/data/" + customModid);
    }
}
