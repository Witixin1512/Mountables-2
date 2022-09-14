package witixin.mountables2.data.files;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.loading.FMLPaths;
import witixin.mountables2.Mountables2Mod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileUtils {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    public static void createPackFromTypeIfNotExists(PackType type) {
        if (type == PackType.CLIENT_RESOURCES) {
            createResourcePackIfNotExists();
        } else {
            createDataPackIfNotExists();
        }
    }

    public static void createResourcePackIfNotExists() {
        final File root = getConfigMountableFolder("assets");
        createDirectoryIfNotExists(root);
        final PackMCMetaTemplate packMcmeta = new PackMCMetaTemplate(root.getParentFile().getParentFile(), "Mountables2 Resources", PackType.CLIENT_RESOURCES);
        packMcmeta.writeIfNotExists();
        createDirectoryIfNotExists(new File(root, "geo"));
        createDirectoryIfNotExists(new File(root, "animations"));
        createDirectoryIfNotExists(new File(root, "textures"));
        createDirectoryIfNotExists(new File(root, "sounds"));
        createSoundsFile(new File(root, "sounds"));
    }

    public static void createDataPackIfNotExists() {
        final File root = getConfigMountableFolder("data");
        createDirectoryIfNotExists(root);
        final PackMCMetaTemplate packMcmeta = new PackMCMetaTemplate(root.getParentFile().getParentFile(), "Mountables2 Data", PackType.SERVER_DATA);
        packMcmeta.writeIfNotExists();
        File mountablesFolder = new File(root, "custom_mountables");
        createDirectoryIfNotExists(mountablesFolder);
        File companionBlock = new File(mountablesFolder, "" +
                "companion_block.json");
        if (!companionBlock.exists() && companionBlock.getParentFile().listFiles().length == 0) {
            try (final PrintWriter writer = new PrintWriter(new FileWriter(companionBlock))) {
                String toWrite = TemplateFile.read(PackType.SERVER_DATA, Mountables2Mod.rl("companion_block"));
                if (toWrite == null || toWrite.isBlank()) throw new RuntimeException("Something went wrong with mountables2 file handling!");
                writer.println(toWrite);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * To be used to find the config subfolder with the {@link PackType#getDirectory()}
     *
     * @param folder The name of the folder to get under the mountables config dir
     * @return A File that may or may not exist.
     */

    public static File getConfigMountableFolder(String folder) {
        return FMLPaths.CONFIGDIR.get().resolve("mountables2/" + folder + "pack/" + folder + "/mountables2").toFile();
    }

    private static void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            try {
                Files.createDirectories(directory.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    private static void createSoundsFile(File rootDir) {
        final TemplateFile templateFile = TemplateFile.of(PackType.SERVER_DATA, new ResourceLocation(Mountables2Mod.MODID, "sounds"));
        List<String> contentStrings = new ArrayList<>();
        for (File f : Objects.requireNonNull(rootDir.listFiles())) {
            if (f.getName().contains(".ogg")) {
                final String fileName = f.getName().split("\\.")[0];
                TemplateFile toManipulate = templateFile.copy();
                toManipulate.setValue("SOUND_TEMPLATE", fileName);
                toManipulate.setValue("RESOURCELOCATION", Mountables2Mod.MODID + ":" + fileName);
                contentStrings.add(toManipulate.getContent());
            }
        }
        File toCheck = new File(rootDir.getParentFile(), "sounds.json");
        if (toCheck.exists()) return;
        try (final PrintWriter writer = new PrintWriter(new FileWriter(toCheck))) {
            String content = "{\n";
            content = content.concat("    \"empty\": {\n" + "        \"sounds\" : [\"mountables2:empty\"]\n" + "    }");
            for (int i = 0; i < contentStrings.size(); i++) {
                content = content.concat(",\n");
                String toAdjoin = contentStrings.get(i);
                content = content.concat("   " + toAdjoin);
            }
            content = content.concat("   \n}");
            writer.println(content);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void writeIfNotExists(File f, PackType type, ResourceLocation toRead) {
        if (!f.exists()) {
            try (final PrintWriter writer = new PrintWriter(new FileWriter(f))) {
                String toWrite = TemplateFile.read(type, toRead);
                if (toWrite == null || toWrite.isBlank()) throw new RuntimeException("Something went wrong with mountables2 file handling!");
                writer.println(toWrite);
            } catch (IOException ignored) {
            }
        }
    }

}
