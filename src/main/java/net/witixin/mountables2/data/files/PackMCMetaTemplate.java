package net.witixin.mountables2.data.files;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.witixin.mountables2.Reference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
public class PackMCMetaTemplate {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */

    //We're in 1.18.2, so format is now 9
    private static final int format = 9;

    private final TemplateFile template;
    private final File mcmetaFile;

    public PackMCMetaTemplate(File containingDirectory, String description) {
        this.template = TemplateFile.of(ResourceType.DATA, new ResourceLocation(Reference.MODID, "pack.mcmeta"));
        template.setValue("PACK_DESCRIPTION", description);
        template.setValue("PACK_FORMAT", String.valueOf(format));
        this.mcmetaFile = new File(containingDirectory, "pack.mcmeta");
    }

    public void writeIfNotExists() {
        if(!mcmetaFile.exists()) {
            try(final PrintWriter writer = new PrintWriter(new FileWriter(mcmetaFile))) {
                String content = template.getContent();
                writer.println(content);
            } catch(IOException e) {
            }
        }
    }
}
