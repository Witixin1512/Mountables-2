package witixin.mountables2.data.files;

public class FileUtils {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */


    public static void writeResourcePack() {
        final ResourcePackInfo resourcePackInfo = ResourcePackInfo.get();
        if (resourcePackInfo == null) {
            return;
        }
        resourcePackInfo.createResourcePackIfNotExists();
    }

    public static void writeDataPack() {
        final ResourcePackInfo resourcePackInfo = ResourcePackInfo.get();
        if (resourcePackInfo == null) {
            return;
        }

        resourcePackInfo.createDataPackIfNotExists();
    }

}
