package net.witixin.mountables2.data.files;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.witixin.mountables2.Reference;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class ResourcePackInfo {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    private final KnownResourceLoaderMod usedModLoader;

    public ResourcePackInfo(KnownResourceLoaderMod usedResourceLoader) {
        this.usedModLoader = usedResourceLoader;
    }

    @Nullable
    public static ResourcePackInfo get() {
        final ModList modList = ModList.get();
        for(KnownResourceLoaderMod value : KnownResourceLoaderMod.values()) {
            if(modList.isLoaded(value.modid)) {
                return new ResourcePackInfo(value);
            }
        }
        return null;
    }

    public void createResourcePackIfNotExists() {
        final File root = usedModLoader.resourcePackDirectory;
        createDirectoryIfNotExists(root);
        final PackMCMetaTemplate packMcmeta = new PackMCMetaTemplate(root.getParentFile().getParentFile(), "Mountables2 Resources");
        packMcmeta.writeIfNotExists();
        createDirectoryIfNotExists(new File(root, "geo"));
        createDirectoryIfNotExists(new File(root, "animations"));
        createDirectoryIfNotExists(new File(root, "textures"));
    }

    public void createDataPackIfNotExists() {
        final File root = usedModLoader.datapackDirectory;
        createDirectoryIfNotExists(root);
        final PackMCMetaTemplate packMcmeta = new PackMCMetaTemplate(root.getParentFile().getParentFile(), "Mountables Data");
        packMcmeta.writeIfNotExists();
        File mountablesFolder = new File(root, "custom_mountables");
        createDirectoryIfNotExists(mountablesFolder);
        writeIfNotExists(new File(mountablesFolder, "companion_block.json"), ResourceType.DATA, Reference.rl("custom_mountables/companion_block"));
    }

    private void createDirectoryIfNotExists(File directory) {
        if(!directory.exists()) {
            try {
                Files.createDirectories(directory.toPath());
            } catch(IOException e) {
            }
        }
    }

    public File getResourcePackDirectory() {
        return usedModLoader.resourcePackDirectory;
    }

    public File getDataPackDirectory() {
        return usedModLoader.datapackDirectory;
    }

    public void writeIfNotExists(File f, ResourceType type, ResourceLocation toRead) {
        if(!f.exists()) {
            try(final PrintWriter writer = new PrintWriter(new FileWriter(f))) {
                String toWrite = TemplateFile.read(type, toRead);
                writer.println(toWrite);
            } catch(IOException e) {
            }
        }
    }
}
