package net.witixin.mountables2.data;


import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public record MountableData(String uniqueName, double width, double height, Double[] position, List<String> emissiveTextures, Boolean[] ai_modes) {

}
