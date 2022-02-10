package net.witixin.mountables2.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;
import net.witixin.mountables2.entity.MountableData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MountableManager extends SimpleJsonResourceReloadListener implements Supplier<List<MountableData>> {

    private static final Gson GSON = new Gson();
    private List<MountableData> mountable_list = new ArrayList<>();


    public MountableManager(String p_10769_) {
        super(GSON, p_10769_);
    }

    @Override
    public List<MountableData> get() {
        return mountable_list;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        mountable_list = new ArrayList<>();
        for (ResourceLocation location : pObject.keySet()){
            try {
                JsonObject obj = GsonHelper.convertToJsonObject(pObject.get(location), "mountables");
                String s = GsonHelper.getAsString(obj, "unique_name");

                JsonArray hitbox_dimensions = GsonHelper.getAsJsonArray(obj, "hitbox");
                double d0 = hitbox_dimensions.get(0).getAsDouble();
                double d1 = hitbox_dimensions.get(1).getAsDouble();

                JsonArray d2 = GsonHelper.getAsJsonArray(obj, "riding_position");
                double posX = d2.get(0).getAsDouble();
                double posY = d2.get(1).getAsDouble();
                double posZ = d2.get(2).getAsDouble();

                JsonArray emissive_textures = GsonHelper.getAsJsonArray(obj, "emissive_textures");
                List<String> list = new ArrayList<>();
                emissive_textures.forEach(jsonElement -> list.add(jsonElement.getAsString()));
                mountable_list.add(new MountableData(s.toLowerCase(), d1, d0, new Double[]{posX, posY, posZ}, list));
            }
            catch (Exception e ) {
                System.out.println("Error parsing mountable data! " + location.toString());
                e.printStackTrace();
            }
        }
    }
}
