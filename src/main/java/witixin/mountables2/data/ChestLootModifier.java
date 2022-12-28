package witixin.mountables2.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import witixin.mountables2.Mountables2Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChestLootModifier extends LootModifier {

    public static final Codec<ChestLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance)
                    .and(instance.group(
                            Codec.INT.fieldOf("weight").forGetter(thing -> thing.weight),
                            Codec.list(Codec.STRING).fieldOf("locations").xmap(
                                    stringList -> stringList.stream().map(ResourceLocation::new).collect(Collectors.toList()),
                                    rlList -> rlList.stream().map(ResourceLocation::toString).collect(Collectors.toList())

                            ).forGetter(thing -> thing.resLocs)
                    )).apply(instance, ChestLootModifier::new));


    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */

    private final int weight;
    private final List<ResourceLocation> resLocs;

    protected ChestLootModifier(LootItemCondition[] conditionsIn, int weight, List<ResourceLocation> resLocs) {
        super(conditionsIn);
        this.resLocs = resLocs;
        this.weight = weight;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
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

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return Mountables2Mod.GLM.get();
    }

}
