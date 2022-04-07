package net.witixin.mountables2.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.witixin.mountables2.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChestLootModifier extends LootModifier {

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    protected ChestLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (isValidResourceLocation(context.getQueriedLootTableId())){
            if (context.getRandom().nextDouble() * 100 <= 5){
                generatedLoot.add(Reference.MYSTERIOUS_FRAGMENT.get().getDefaultInstance());
                if (context.getRandom().nextDouble() > 1.0/2){
                    generatedLoot.add(Reference.MYSTERIOUS_FRAGMENT.get().getDefaultInstance());
                }
                if (context.getRandom().nextDouble() > 1.0/4){
                    generatedLoot.add(Reference.MYSTERIOUS_FRAGMENT.get().getDefaultInstance());
                }
            }
        }
        return generatedLoot;
    }

    private boolean isValidResourceLocation(ResourceLocation rl){
        final String toCheck = rl.toString();
        return toCheck.contains("stronghold") || toCheck.contains("bastion") || toCheck.contains("mineshaft") || toCheck.contains("end_city");
    }

    public static class ChestLootModifierSerializer extends GlobalLootModifierSerializer<ChestLootModifier> {
        @Override
        public ChestLootModifier read(ResourceLocation name, JsonObject json, LootItemCondition[] conditionsIn) {
            return new ChestLootModifier(conditionsIn);
        }

        @Override
        public JsonObject write(ChestLootModifier instance) {
            return makeConditions(instance.conditions);
        }
    }
}
