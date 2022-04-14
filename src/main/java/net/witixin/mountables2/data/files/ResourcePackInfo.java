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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        createDirectoryIfNotExists(new File(root, "sounds"));
        createSoundsFile(new File(root, "sounds"));
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
            } catch(IOException ignored) {
            }
        }
    }
    private void createSoundsFile(File rootDir){
        final TemplateFile templateFile = TemplateFile.of(ResourceType.DATA, new ResourceLocation(Reference.MODID, "sounds"));
        List<String> contentStrings = new ArrayList<>();
        for (File f : Objects.requireNonNull(rootDir.listFiles())){
            if (f.getName().contains(".ogg")){
                final String fileName = f.getName().split("\\.")[0];
                TemplateFile toManipulate = templateFile.copy();
                toManipulate.setValue("SOUND_TEMPLATE", fileName);
                toManipulate.setValue("RESOURCELOCATION", Reference.MODID + ":" + fileName);
                contentStrings.add(toManipulate.getContent());
            }
        }
        File toCheck = new File(rootDir.getParentFile(), "sounds.json");
        if (toCheck.exists())return;
        try(final PrintWriter writer = new PrintWriter(new FileWriter(toCheck))) {
            String content = "{\n";
            content = content.concat("    \"empty\": {\n" +"        \"sounds\" : [\"mountables2:empty\"]\n" +"    }, \n");
            for (int i = 0; i < contentStrings.size(); i++){
                String toAdjoin = contentStrings.get(i);
                content = content.concat("   " + toAdjoin);
                if (!(i == contentStrings.size() -1)){
                    content = content.concat(",\n");
                }
            }
            content = content.concat("   \n}");
            writer.println(content);
        } catch(IOException e) {
            System.out.println(e.getMessage());
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
            } catch(IOException ignored) {
            }
        }
    }
}
