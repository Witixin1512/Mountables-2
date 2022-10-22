package witixin.mountables2.data;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import witixin.mountables2.Mountables2Mod;

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
public record MountableData(ResourceLocation recipeName, String uniqueName, double width, double height, Double[] position, List<String> emissiveTextures, Boolean[] aiModes, String displayName, Map<String, Double> attributeMap) implements Recipe<Container> {

    public static final MountableSerializer MOUNTABLE_SERIALIZER = new MountableSerializer();

    public double getAttributeValue(AttributeMap value) {
        return this.attributeMap().get(value.name());
    }

    @Override
    public String toString() {
        return "MountableData[ " + uniqueName + ", Hitbox: " + width + " , " + height + " Position: " + Arrays.toString(position) + " EmissiveTextures: " + Arrays.toString(emissiveTextures.toArray(emissiveTextures().toArray(new String[emissiveTextures.size()]))) + " AI Modes: " + Arrays.toString(aiModes) + " Display Name: " + displayName + "Attributes: " + attributeMap.toString();
    }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(Container pContainer) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeName;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MOUNTABLE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return Mountables2Mod.MOUNTABLE_RECIPE_TYPE;
    }

    public enum AttributeMap {
        MOVEMENT_SPEED,
        JUMP_STRENGTH,
        MAX_HEALTH,
        FLYING_SPEED,
        FOLLOW_RANGE
    }
}
