package witixin.mountables2.data.files;

import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Reference;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateFile {

    /**
     * Credit to this code goes to Jared, SkySom, Jared and kindlich
     * This code was taken from the ContentTweaker mod.
     */


    public static final TemplateFile EMPTY = new TemplateFile(new ResourceLocation(Reference.MODID, "templates/empty"), "") {
        @Override
        public void setValue(String key, String value) {
        }
    };
    private static final String delimiter = "$$";
    private final ResourceLocation location;
    private String template;

    private TemplateFile(ResourceLocation location, String template) {
        this.template = template;
        this.location = location;
    }

    public static TemplateFile of(ResourceType type, ResourceLocation location) {
        final String template = read(type, location);
        if (template == null) {
            return EMPTY;
        }
        return new TemplateFile(location, template);
    }

    public static String read(ResourceType type, ResourceLocation location) {
        final String format = "/%s/%s/%s.json";
        final String folderName = type.getFolderName();
        final String path = String.format(format, folderName, location.getNamespace(), location.getPath());

        final InputStream resourceAsStream = TemplateFile.class.getResourceAsStream(path);
        if (resourceAsStream == null) {
            return null;
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public TemplateFile copy() {
        return this == EMPTY ? this : new TemplateFile(location, template);
    }

    public void setValues(Map<String, String> keyValuePairs) {
        keyValuePairs.forEach(this::setValue);
    }

    public void setValue(String key, String value) {
        final String embeddedKey = delimiter + key + delimiter;
        if (!template.contains(embeddedKey)) {
            throw new IllegalArgumentException(String.format("Invalid key for template %s: '%s'", location, key));
        }
        template = template.replace(embeddedKey, value);
    }

    public byte[] getContentArray() {
        return template.getBytes();
    }

    public String getContent() {
        return template;
    }

}