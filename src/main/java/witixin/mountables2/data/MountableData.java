package witixin.mountables2.data;


import net.minecraft.core.RegistryAccess;
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
import java.util.Objects;

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

    @Override
    public String toString() {
        return "MountableData[ " + uniqueName + ", Hitbox: " + width + " , " + height + " Position: " + Arrays.toString(position) + " EmissiveTextures: " + Arrays.toString(emissiveTextures.toArray(emissiveTextures().toArray(new String[emissiveTextures.size()]))) + " AI Modes: " + Arrays.toString(aiModes) + " Display Name: " + displayName + "Attributes: " + attributeMap.toString();
    }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_){
        return ItemStack.EMPTY;
    }


    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeName;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Mountables2Mod.MOUNTABLE_RECIPE_SERAILIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Mountables2Mod.MOUNTABLE_RECIPE_TYPE.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MountableData that = (MountableData) o;

        if (Double.compare(that.width, width) != 0) return false;
        if (Double.compare(that.height, height) != 0) return false;
        if (!Objects.equals(recipeName, that.recipeName)) return false;
        if (!Objects.equals(uniqueName, that.uniqueName)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(position, that.position)) return false;
        if (!Objects.equals(emissiveTextures, that.emissiveTextures))
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(aiModes, that.aiModes)) return false;
        if (!Objects.equals(displayName, that.displayName)) return false;
        return Objects.equals(attributeMap, that.attributeMap);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = recipeName != null ? recipeName.hashCode() : 0;
        result = 31 * result + (uniqueName != null ? uniqueName.hashCode() : 0);
        temp = Double.doubleToLongBits(width);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(height);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(position);
        result = 31 * result + (emissiveTextures != null ? emissiveTextures.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(aiModes);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (attributeMap != null ? attributeMap.hashCode() : 0);
        return result;
    }
}
