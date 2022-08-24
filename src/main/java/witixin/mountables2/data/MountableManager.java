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
        for (ResourceLocation location : pObject.keySet()){
            try {
                JsonObject obj = GsonHelper.convertToJsonObject(pObject.get(location), location.toString());
                loadMountable(obj);
            }
            catch (Exception e ) {
                System.out.println("Error parsing mountable data for resource location: " + location.toString());
                e.printStackTrace();
            }
        }
        if (mountable_list.isEmpty()){
            JsonObject obj = new JsonObject();
            obj.addProperty("unique_name", "companion_block");
            obj.addProperty("display_name", "Companion");
            JsonArray hitbox = new JsonArray();
            hitbox.add(1.0);
            hitbox.add(1.0);
            obj.add("hitbox",  hitbox);
            JsonArray ridingPos = new JsonArray();
            ridingPos.add(0.0);
            ridingPos.add(-0.1);
            ridingPos.add(0.0);
            obj.add("riding_position", ridingPos);
            obj.addProperty("canSwim", true);
            obj.addProperty("canFly", true);
            obj.addProperty("canWalk", true);
            JsonArray emissiveTexxes = new JsonArray();
            emissiveTexxes.add("companion_block_emissive");
            emissiveTexxes.add("companion_block_translucent");
            obj.add("emissive_textures", emissiveTexxes);
            JsonObject attributes = new JsonObject();
            attributes.addProperty("MOVEMENT_SPEED", 0.5);
            attributes.addProperty("JUMP_STRENGTH", 0.5);
            attributes.addProperty("MAX_HEALTH", 20);
            attributes.addProperty("FLYING_SPEED", 1);
            attributes.addProperty("FOLLOW_RANGE", 20);
            obj.add("attributes", attributes);
            loadMountable(obj);
        }
    }
    private void loadMountable(JsonObject obj){
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

        boolean b3 = GsonHelper.getAsBoolean(obj, "canFly");
        boolean b1 = GsonHelper.getAsBoolean(obj, "canSwim");
        boolean b2 = GsonHelper.getAsBoolean(obj, "canWalk");


        String displayName = GsonHelper.getAsString(obj, "display_name");
        JsonObject secondObject = GsonHelper.getAsJsonObject(obj, "attributes");
        Map<String, Double> attributeMap = new HashMap<>();
        secondObject.entrySet().forEach(entry -> attributeMap.put(entry.getKey(), entry.getValue().getAsDouble()));
        mountable_list.add(new MountableData(s.toLowerCase(), d0, d1, new Double[]{posX, posY, posZ}, list, new Boolean[]{b1, b2, b3}, displayName, attributeMap));
        LogManager.getLogger("mountables2").info("Registered a new mountable under the name: " + s);
    }
}
