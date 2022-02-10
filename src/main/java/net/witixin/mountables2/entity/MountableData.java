package net.witixin.mountables2.entity;


import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public record MountableData(String uniqueName, double width, double height, Double[] position, List<String> emissiveTextures) {

    public static CompoundTag listToNBT(List<MountableData> listData) {
        CompoundTag tag = new CompoundTag();
        for (MountableData data :listData){
            CompoundTag toInsert = new CompoundTag();
            toInsert.putString("emissive_textures", data.emissiveTextures.toString());
            tag.put(data.uniqueName(), toInsert);
        }
        return tag;
    }
    public static List<String> toStringList(List<MountableData> data){
        List<String> toReturn = new ArrayList<>();
        data.forEach(datamember -> toReturn.add(datamember.uniqueName().toUpperCase()));
        return toReturn;
    }
}
