package net.witixin.mountables2;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.witixin.mountables2.data.ChestLootModifier;
import net.witixin.mountables2.data.MountableManager;
import net.witixin.mountables2.data.files.FileUtils;
import net.witixin.mountables2.entity.Mountable;
import net.witixin.mountables2.data.MountableData;
import net.witixin.mountables2.item.CommandChip;
import net.witixin.mountables2.item.MountableItem;
import net.witixin.mountables2.network.PacketHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Random;
import java.util.function.Supplier;


@Mod(Reference.MODID)
public class Reference {
    public static final String MODID = "mountables2";
    public static final MountableManager MOUNTABLE_MANAGER = new MountableManager("custom_mountables");

    //TODO WIKI!

    public static final CreativeModeTab CREATIVE_MODE_TAB = new CreativeModeTab(CreativeModeTab.getGroupCountSafe(), "mountables2.ctab") {
        @Override
        public ItemStack makeIcon() {
            return MOUNTABLE.get().getDefaultInstance();
        }
    };

    private static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().tab(CREATIVE_MODE_TAB);

    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


    public static final RegistryObject<Item> MYSTERIOUS_FRAGMENT = ITEM_REGISTER.register("mysterious_fragment",
            () -> {
                return new Item(DEFAULT_PROPERTIES.fireResistant()) {
                    @Override
                    public boolean canBeHurtBy(DamageSource pDamageSource) {
                        return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
                    }
                };
            });


    public static final RegistryObject<Item> MOUNTABLE_CORE = ITEM_REGISTER.register("mountable_core",
            () -> {
                return new Item(DEFAULT_PROPERTIES.stacksTo(1).fireResistant());
            });


    public static final RegistryObject<Item> MOUNTABLE = ITEM_REGISTER.register("mountable",
            () -> {
                return new MountableItem(DEFAULT_PROPERTIES.stacksTo(1)) {
                    @Override
                    public boolean canBeHurtBy(DamageSource pDamageSource) {
                        return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
                    }
                };
            });

    public static final RegistryObject<Item> COMMAND_CHIP = ITEM_REGISTER.register("command_chip",
            () -> new CommandChip(DEFAULT_PROPERTIES.stacksTo(1).fireResistant()));

    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    public static final RegistryObject<EntityType<Mountable>> MOUNTABLE_ENTITY = ENTITY_REGISTER.register("mountable_entity",
            () -> EntityType.Builder.of(Mountable::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(10).build("mountable_entity"));

    private static final DeferredRegister<GlobalLootModifierSerializer<?>> GLM_REG = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, MODID);
    private static final RegistryObject<ChestLootModifier.ChestLootModifierSerializer> GLM = GLM_REG.register("fragment_modifier", ChestLootModifier.ChestLootModifierSerializer::new);

    public Reference() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.GENERAL_SPEC, "mountables2.toml");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::attributeCreation);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        GLM_REG.register(bus);
        MinecraftForge.EVENT_BUS.addListener(this::onDataPackLoad);
        MinecraftForge.EVENT_BUS.addListener(this::resizeEntity);
        MinecraftForge.EVENT_BUS.addListener(this::villagerEvent);
        PacketHandler.init();
    }

    public static ResourceLocation rl(String s) {
        return new ResourceLocation(MODID, s);
    }


    private void onDataPackLoad(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                return null;
            }

            @Override
            protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                FileUtils.setupResourceAndDataPacks();
            }
        });
        //MOUNTABLE_MANAGER is a
        event.addListener(MOUNTABLE_MANAGER);
    }

    public static MountableData findData(String unique_name) {
        return MOUNTABLE_MANAGER.get().stream().filter(e -> e.uniqueName().matches(unique_name)).findFirst().get();
    }

    public void villagerEvent(final WandererTradesEvent event) {
        event.getRareTrades().add(FRAGMENT_TRADE.get());
    }

    private void attributeCreation(final EntityAttributeCreationEvent event) {
        event.put(MOUNTABLE_ENTITY.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FLYING_SPEED, 0).add(Attributes.FOLLOW_RANGE, 10).add(Attributes.ATTACK_DAMAGE, 0).add(Attributes.JUMP_STRENGTH, 0.5).add(Attributes.FLYING_SPEED, 0.5).build());
    }

    private void resizeEntity(final EntityEvent.Size event) {
        if (event.getEntity() instanceof Mountable mountable) {
            MountableData mountableData = mountable.getMountableData();
            if (mountableData != null) {
                event.setNewSize(EntityDimensions.fixed((float) mountableData.width(), (float) mountableData.height()));
                mountable.setBoundingBox(event.getNewSize().makeBoundingBox(1.0D, 1.0D, 1.0D));
            }
        }
    }

    private static final Supplier<VillagerTrades.ItemListing> FRAGMENT_TRADE = () -> new VillagerTrades.ItemListing() {
        private final MerchantOffer toReturn = new MerchantOffer(new ItemStack(Items.EMERALD, 5), MYSTERIOUS_FRAGMENT.get().getDefaultInstance(), 1, 1, 0.05f);

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity pTrader, Random pRand) {
            return toReturn;
        }
    };
}

