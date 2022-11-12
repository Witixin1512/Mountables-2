package witixin.mountables2.data.files;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import witixin.mountables2.Mountables2Mod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PackMCMetaTemplate {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */


    private final TemplateFile template;
    private final File mcmetaFile;

    public PackMCMetaTemplate(File containingDirectory, String description, PackType type) {
        this.template = TemplateFile.of(PackType.SERVER_DATA, new ResourceLocation(Mountables2Mod.MODID, "pack.mcmeta"));
        template.setValue("PACK_DESCRIPTION", description);
        template.setValue("PACK_FORMAT", String.valueOf(type.getVersion(SharedConstants.getCurrentVersion())));
        this.mcmetaFile = new File(containingDirectory, "pack.mcmeta");
    }

    public void writeIfNotExists() {
        if (!mcmetaFile.exists()) {
            try (final PrintWriter writer = new PrintWriter(new FileWriter(mcmetaFile))) {
                String content = template.getContent();
                writer.println(content);
            } catch (IOException e) {

            }
        }
    }
}
