package net.witixin.mountables2.data.files;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FileUtils {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    public static void setupResourceAndDataPacks(){
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FileUtils::writeResourcePack);
        writeDataPack();
    }


    private static void writeResourcePack() {
        final ResourcePackInfo resourcePackInfo = ResourcePackInfo.get();
        if (resourcePackInfo == null) {
            return;
        }
        resourcePackInfo.createResourcePackIfNotExists();
    }

    private static void writeDataPack() {
        final ResourcePackInfo resourcePackInfo = ResourcePackInfo.get();
        if (resourcePackInfo == null) {
            return;
        }

        resourcePackInfo.createDataPackIfNotExists();
    }

}
