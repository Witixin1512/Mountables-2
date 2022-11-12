package witixin.mountables2.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MountableManager extends SimpleJsonResourceReloadListener {

    public static final int SWIM = 0;
    public static final int WALK = 1;
    public static final int FLY = 2;
    private static final Gson GSON = new Gson();
    private static List<MountableData> mountable_list = new ArrayList<>();

    public MountableManager(String p_10769_) {
        super(GSON, p_10769_);
    }

    public static List<MountableData> get() {
        return mountable_list;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        mountable_list = new ArrayList<>();
        for (ResourceLocation location : pObject.keySet()) {
            try {
                JsonObject obj = GsonHelper.convertToJsonObject(pObject.get(location), location.toString());
                loadMountable(obj);
            } catch (Exception e) {
                System.out.println("Error parsing mountable data for resource location: " + location.toString());
                e.printStackTrace();
            }
        }
    }

    private void loadMountable(JsonObject obj) {
        String getUniqueName = GsonHelper.getAsString(obj, "unique_name");

        JsonArray hitboxArray = GsonHelper.getAsJsonArray(obj, "hitbox");
        double hitboxX = hitboxArray.get(0).getAsDouble();
        double hitboxY = hitboxArray.get(1).getAsDouble();

        JsonArray positionArray = GsonHelper.getAsJsonArray(obj, "riding_position");
        Double[] riderPosition = new Double[3];
        riderPosition[0] = positionArray.get(0).getAsDouble();
        riderPosition[1] = positionArray.get(1).getAsDouble();
        riderPosition[2] = positionArray.get(2).getAsDouble();

        Boolean[] canMovement = new Boolean[3];
        canMovement[SWIM] = GsonHelper.getAsBoolean(obj, "canSwim");
        canMovement[FLY] = GsonHelper.getAsBoolean(obj, "canFly");
        canMovement[WALK] = GsonHelper.getAsBoolean(obj, "canWalk");

        JsonArray emissive_textures = GsonHelper.getAsJsonArray(obj, "emissive_textures");
        List<String> list = new ArrayList<>();
        emissive_textures.forEach(jsonElement -> list.add(jsonElement.getAsString()));

        String displayName = GsonHelper.getAsString(obj, "display_name");
        JsonObject jsonAttributesMap = GsonHelper.getAsJsonObject(obj, "attributes");

        Map<String, Double> attributeMap = new HashMap<>();
        //preferably use a loop here, streams may be less effective with small itterations
        for (Map.Entry<String, JsonElement> entries : jsonAttributesMap.entrySet())
            attributeMap.put(entries.getKey(), entries.getValue().getAsDouble());

        mountable_list.add(new MountableData(getUniqueName.toLowerCase(), hitboxX, hitboxY, riderPosition, list, canMovement, displayName, attributeMap));
        LogManager.getLogger("mountables2").info("Registered a new mountable under the name: " + getUniqueName);
    }
}
