package witixin.mountables2.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;
import witixin.mountables2.Mountables2Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MountableSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>  implements RecipeSerializer<MountableData> {

    public static final int SWIM = 0;
    public static final int WALK = 1;
    public static final int FLY = 2;

    public static List<MountableData> dataList = new ArrayList<>();

    public MountableSerializer(){
        this.setRegistryName(new ResourceLocation(Mountables2Mod.MODID,"custom_mountables"));
    }


    @Override
    public MountableData fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

        JsonArray jsonArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "hitbox");
        JsonArray positionArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "riding_position");

        Double[] riderPosition = new Double[3];
        riderPosition[0] = positionArray.get(0).getAsDouble();
        riderPosition[1] = positionArray.get(1).getAsDouble();
        riderPosition[2] = positionArray.get(2).getAsDouble();

        Boolean[] movementAbilityArray = new Boolean[3];
        movementAbilityArray[SWIM] = GsonHelper.getAsBoolean(pSerializedRecipe, "canSwim");
        movementAbilityArray[FLY] = GsonHelper.getAsBoolean(pSerializedRecipe, "canFly");
        movementAbilityArray[WALK] = GsonHelper.getAsBoolean(pSerializedRecipe, "canWalk");

        JsonArray emissive_textures = GsonHelper.getAsJsonArray(pSerializedRecipe, "emissive_textures");
        List<String> emissiveTexturesList = new ArrayList<>();
        emissive_textures.forEach(jsonElement -> emissiveTexturesList.add(jsonElement.getAsString()));

        String displayName = GsonHelper.getAsString(pSerializedRecipe, "display_name");
        JsonObject jsonAttributesMap = GsonHelper.getAsJsonObject(pSerializedRecipe, "attributes");

        Map<String, Double> attributeMap = new HashMap<>();
        //preferably use a loop here, streams may be less effective with small itterations
        for (Map.Entry<String, JsonElement> entries : jsonAttributesMap.entrySet())
            attributeMap.put(entries.getKey(), entries.getValue().getAsDouble());


        return new MountableData(pRecipeId,
                GsonHelper.getAsString(pSerializedRecipe, "unique_name"),
                jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble(),
                riderPosition, emissiveTexturesList, movementAbilityArray, displayName,
                attributeMap
                );
    }

    @Nullable
    @Override
    public MountableData fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        return new MountableData(new ResourceLocation(pBuffer.readUtf()),
                pBuffer.readUtf(),
                pBuffer.readDouble(),
                pBuffer.readDouble(),
                new Double[]{pBuffer.readDouble(), pBuffer.readDouble(), pBuffer.readDouble()},
                pBuffer.readCollection(ArrayList::new, FriendlyByteBuf::readUtf),
                new Boolean[]{pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readBoolean()},
                pBuffer.readUtf(),
                pBuffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readDouble)
                );
    }

    @Override
    public void toNetwork(FriendlyByteBuf pBuffer, MountableData pRecipe) {
        pBuffer.writeUtf(pRecipe.recipeName().toString());
        pBuffer.writeUtf(pRecipe.uniqueName());
        pBuffer.writeDouble(pRecipe.width());
        pBuffer.writeDouble(pRecipe.height());
        Double[] positionArray = pRecipe.position();
        pBuffer.writeDouble(positionArray[0]);
        pBuffer.writeDouble(positionArray[1]);
        pBuffer.writeDouble(positionArray[2]);
        pBuffer.writeCollection(pRecipe.emissiveTextures(), FriendlyByteBuf::writeUtf);
        Boolean[] movementAiArray = pRecipe.aiModes();
        pBuffer.writeBoolean(movementAiArray[0]);
        pBuffer.writeBoolean(movementAiArray[1]);
        pBuffer.writeBoolean(movementAiArray[2]);
        pBuffer.writeUtf(pRecipe.displayName());
        pBuffer.writeMap(pRecipe.attributeMap(), FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeDouble);
    }
}
