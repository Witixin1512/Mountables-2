package witixin.mountables2;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import witixin.mountables2.data.ChestLootModifier;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.data.MountableSerializer;
import witixin.mountables2.data.files.FileUtils;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.MovementRegistry;
import witixin.mountables2.item.CommandChip;
import witixin.mountables2.item.MountableItem;
import witixin.mountables2.network.PacketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


@Mod(Mountables2Mod.MODID)
public class Mountables2Mod {
    public static final String MODID = "mountables2";

    //TODO WIKI!
    /*
     */
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final RegistryObject<EntityType<Mountable>> MOUNTABLE_ENTITY = ENTITY_REGISTER.register("mountable_entity",
            () -> EntityType.Builder.of(Mountable::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(10).fireImmune().noSummon().build("mountable_entity"));
    private static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties();
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_REG = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final RegistryObject<Codec<ChestLootModifier>> GLM =
            GLM_REG.register("fragment_modifier", () -> ChestLootModifier.CODEC);

    private static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<Item> MYSTERIOUS_FRAGMENT = ITEM_REGISTER.register("mysterious_fragment",
            () -> new Item(DEFAULT_PROPERTIES.fireResistant()) {
                @Override
                public boolean canBeHurtBy(DamageSource pDamageSource) {
                    return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
                }
            });
    public static final RegistryObject<SoundEvent> EMPTY_SOUND_EVENT = SOUND_REGISTER.register("empty", () -> SoundEvent.createVariableRangeEvent(rl("empty")));


    public Mountables2Mod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        GLM_REG.register(bus);
        SOUND_REGISTER.register(bus);
        RECIPE_SER_REG.register(bus);
        RECIPE_TYPE_REG.register(bus);
        bus.addListener(this::attributeCreation);;
        bus.addListener(this::addResourcePackListener);
        MinecraftForge.EVENT_BUS.addListener(this::onDataPackLoad);
        MinecraftForge.EVENT_BUS.addListener(this::villagerEvent);
        PacketHandler.init();
        MovementRegistry.INSTANCE.load();
    }

    public static final RegistryObject<Item> MOUNTABLE_CORE = ITEM_REGISTER.register("mountable_core",
            () -> new Item(DEFAULT_PROPERTIES.stacksTo(1)));

    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER_REG = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<MountableSerializer> MOUNTABLE_RECIPE_SERAILIZER = RECIPE_SER_REG.register("custom_mountables", MountableSerializer::new);

    public static DeferredRegister<RecipeType<?>> RECIPE_TYPE_REG = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final RegistryObject<RecipeType<MountableData>> MOUNTABLE_RECIPE_TYPE = RECIPE_TYPE_REG.register("custom_mountables", () -> RecipeType.simple(new ResourceLocation(MODID, "custom_mountables")));


    private void attributeCreation(final EntityAttributeCreationEvent event) {
        event.put(MOUNTABLE_ENTITY.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FLYING_SPEED, 0).add(Attributes.FOLLOW_RANGE, 10).add(Attributes.ATTACK_DAMAGE, 0).add(Attributes.JUMP_STRENGTH, 1.4).add(Attributes.FLYING_SPEED, 0.5).build());
    }

    private void onDataPackLoad(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                return null;
            }

            @Override
            protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                FileUtils.createDataPackIfNotExists();
            }
        });
    }

    public static final RegistryObject<Item> MOUNTABLE = ITEM_REGISTER.register("mountable",
            () -> new MountableItem(DEFAULT_PROPERTIES.stacksTo(1)) {
                @Override
                public boolean canBeHurtBy(DamageSource pDamageSource) {
                    return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
                }
            });

    private void addResourcePackListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                return null;
            }

            @Override
            protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                FileUtils.createResourcePackIfNotExists();
            }
        });
    }

    public void villagerEvent(final WandererTradesEvent event) {
        event.getRareTrades().add(FRAGMENT_TRADE.get());
    }

    public static final RegistryObject<Item> COMMAND_CHIP = ITEM_REGISTER.register("command_chip",
            () -> new CommandChip(DEFAULT_PROPERTIES.stacksTo(1).fireResistant())  {
                @Override
                public boolean canBeHurtBy(DamageSource pDamageSource) {
                    return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
                }
            } );

    public static ResourceLocation rl(String s) {
        return new ResourceLocation(MODID, s);
    }

    /**
     * Only called on the ServerSide
     * @param unique_name The unique_name of the mountable data to find.
     * @param server The MinecraftServer we are looking in
     * @return The MountableData with the right unique_name
     */
    public static MountableData findData(String unique_name, MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MOUNTABLE_RECIPE_TYPE.get()).stream().filter(recipe -> recipe.uniqueName().equals(unique_name)).findAny().get();
    }


    private static final Supplier<VillagerTrades.ItemListing> FRAGMENT_TRADE = () -> new VillagerTrades.ItemListing() {
        private final MerchantOffer toReturn = new MerchantOffer(new ItemStack(Items.EMERALD, 5), MYSTERIOUS_FRAGMENT.get().getDefaultInstance(), 1, 1, 0.05f);

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource pRand) {
            return toReturn;
        }
    };

}

