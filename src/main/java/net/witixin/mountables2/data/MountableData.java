package net.witixin.mountables2.data;


import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record MountableData(String uniqueName, double width, double height, Double[] position, List<String> emissiveTextures, Boolean[] ai_modes, String displayName, Map<String, Double> attributeMap) {

    @Override
    public String toString(){
        return "MountableData[ " + uniqueName + ", Hitbox: " + width + " , " + height + " Position: " + Arrays.toString(position) + " EmissiveTextures: " + Arrays.toString(emissiveTextures.toArray(emissiveTextures().toArray(new String[emissiveTextures.size()]))) + " AI Modes: " + Arrays.toString(ai_modes) + " Display Name: " + displayName + "Attributes: " + attributeMap.toString();
    }
}
