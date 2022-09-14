package witixin.mountables2.data;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Mountable Data is a record that stores the JSON contents of a Mountable, when registered through a datapack.
 *
 * @param uniqueName
 * @param width
 * @param height
 * @param position
 * @param emissiveTextures
 * @param aiModes
 * @param displayName
 * @param attributeMap
 */
public record MountableData(String uniqueName, double width, double height, Double[] position, List<String> emissiveTextures, Boolean[] aiModes, String displayName, Map<String, Double> attributeMap) {
    public double getAttributeValue(AttributeMap value) {
        return this.attributeMap().get(value.name());
    }

    @Override
    public String toString() {
        return "MountableData[ " + uniqueName + ", Hitbox: " + width + " , " + height + " Position: " + Arrays.toString(position) + " EmissiveTextures: " + Arrays.toString(emissiveTextures.toArray(emissiveTextures().toArray(new String[emissiveTextures.size()]))) + " AI Modes: " + Arrays.toString(aiModes) + " Display Name: " + displayName + "Attributes: " + attributeMap.toString();
    }

    public enum AttributeMap {
        MOVEMENT_SPEED,
        JUMP_STRENGTH,
        MAX_HEALTH,
        FLYING_SPEED,
        FOLLOW_RANGE
    }
}
