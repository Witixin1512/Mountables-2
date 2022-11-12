package witixin.mountables2.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import witixin.mountables2.Mountables2Mod;

import java.util.ArrayList;
import java.util.List;

public class ChestLootModifier extends LootModifier {

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */

    private final int weight;
    private final List<ResourceLocation> resLocs;

    protected ChestLootModifier(LootItemCondition[] conditionsIn, int weight, List<String> resLocs) {
        super(conditionsIn);
        List<ResourceLocation> list = new ArrayList<>();
        for (String s : resLocs) {
            try {
                ResourceLocation rl = new ResourceLocation(s);
                list.add(rl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.resLocs = list;
        this.weight = weight;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (resLocs.contains(context.getQueriedLootTableId())) {
            if (context.getRandom().nextDouble() * 100 <= weight) {
                generatedLoot.add(Mountables2Mod.MYSTERIOUS_FRAGMENT.get().getDefaultInstance());
                if (context.getRandom().nextDouble() <= 1.0 / 2) {
                    generatedLoot.add(Mountables2Mod.MYSTERIOUS_FRAGMENT.get().getDefaultInstance());
                }
                if (context.getRandom().nextDouble() <= 1.0 / 4) {
                    generatedLoot.add(Mountables2Mod.MYSTERIOUS_FRAGMENT.get().getDefaultInstance());
                }
            }
        }
        return generatedLoot;
    }

    public static class ChestLootModifierSerializer extends GlobalLootModifierSerializer<ChestLootModifier> {
        @Override
        public ChestLootModifier read(ResourceLocation name, JsonObject json, LootItemCondition[] conditionsIn) {
            int weight = GsonHelper.getAsInt(json, "weight");
            List<String> resLocs = new ArrayList<>();
            GsonHelper.getAsJsonArray(json, "locations").forEach(jsonElement -> resLocs.add(jsonElement.getAsString()));
            return new ChestLootModifier(conditionsIn, weight, resLocs);
        }

        @Override
        public JsonObject write(ChestLootModifier instance) {
            JsonObject toReturn = makeConditions(instance.conditions);
            toReturn.addProperty("weight", instance.weight);
            JsonArray array = new JsonArray(instance.resLocs.size());
            instance.resLocs.forEach(element -> array.add(element.toString()));
            toReturn.add("locations", array);
            return toReturn;
        }
    }
}
