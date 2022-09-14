package witixin.mountables2;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
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
import witixin.mountables2.data.MountableManager;
import witixin.mountables2.data.files.FileUtils;
import witixin.mountables2.entity.newmountable.Mountable;
import witixin.mountables2.entity.newmountable.movement.MovementRegistry;
import witixin.mountables2.item.CommandChip;
import witixin.mountables2.item.MountableItem;
import witixin.mountables2.network.PacketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;


@Mod(Mountables2Mod.MODID)
public class Mountables2Mod {
    public static final String MODID = "mountables2";

    //TODO WIKI!
    /*
       4 files
       Json -> IDs
       Model -> Key model (Multiple models / mount - meh)
       Anim -> Naming
       Texture -> Naming

       Optionals:
       Sounds (link to fcw)
       Emissive texxes

       Naming
       GeckoLib wiki
     */
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);    /*
    - riding a mount with fly mode set to hop provides no forward movement, all buttons produce a jump instead although I can't reproduce it, probably something to do with the swapping bug
    - jump and land animations do not play
    - mount plays fly animation in air when not flying
    - can't adjust seat position, speeds, jump strength on mounts
     */
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    public static final CreativeModeTab CREATIVE_MODE_TAB = new CreativeModeTab(CreativeModeTab.getGroupCountSafe(), "mountables2.ctab") {
        @Override
        public ItemStack makeIcon() {
            return MOUNTABLE.get().getDefaultInstance();
        }
    };
    public static final RegistryObject<EntityType<Mountable>> MOUNTABLE_ENTITY = ENTITY_REGISTER.register("mountable_entity",
            () -> EntityType.Builder.of(Mountable::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(10).build("mountable_entity"));
    public static final Map<String, String> SRG_ATTRIBUTES_MAP = Util.make(() -> {
        Map<String, String> srgMap = new HashMap<>();
        srgMap.put("MAX_HEALTH", "f_22276_");
        srgMap.put("FOLLOW_RANGE", "f_22277_");
        srgMap.put("MOVEMENT_SPEED", "f_22279_");
        srgMap.put("FLYING_SPEED", "f_22279_");
        srgMap.put("JUMP_STRENGTH", "f_22288_");
        return srgMap;
    });
    private static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().tab(CREATIVE_MODE_TAB);
    private static final DeferredRegister<GlobalLootModifierSerializer<?>> GLM_REG = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, MODID);
    private static final RegistryObject<ChestLootModifier.ChestLootModifierSerializer> GLM = GLM_REG.register("fragment_modifier", ChestLootModifier.ChestLootModifierSerializer::new);
    private static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<Item> MYSTERIOUS_FRAGMENT = ITEM_REGISTER.register("mysterious_fragment",
            () -> new Item(DEFAULT_PROPERTIES.fireResistant()) {
                @Override
                public boolean canBeHurtBy(DamageSource pDamageSource) {
                    return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
                }
            });
    public static final RegistryObject<SoundEvent> EMPTY_SOUND_EVENT = SOUND_REGISTER.register("empty", () -> new SoundEvent(rl("empty")));

    public Mountables2Mod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::attributeCreation);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        GLM_REG.register(bus);
        SOUND_REGISTER.register(bus);
        MinecraftForge.EVENT_BUS.addListener(this::onDataPackLoad);
        bus.addListener(this::addResourcePackListener);
        MinecraftForge.EVENT_BUS.addListener(this::villagerEvent);
        PacketHandler.init();
        MovementRegistry.INSTANCE.load();

    }

    public static final RegistryObject<Item> MOUNTABLE_CORE = ITEM_REGISTER.register("mountable_core",
            () -> new Item(DEFAULT_PROPERTIES.stacksTo(1).fireResistant()));

    private void attributeCreation(final EntityAttributeCreationEvent event) {
        event.put(MOUNTABLE_ENTITY.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FLYING_SPEED, 0).add(Attributes.FOLLOW_RANGE, 10).add(Attributes.ATTACK_DAMAGE, 0).add(Attributes.JUMP_STRENGTH, 0.5).add(Attributes.FLYING_SPEED, 0.5).build());
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
        //MOUNTABLE_MANAGER is a SimpleJSONReloadListener
        event.addListener(new MountableManager("custom_mountables"));
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
            () -> new CommandChip(DEFAULT_PROPERTIES.stacksTo(1).fireResistant()));

    public static ResourceLocation rl(String s) {
        return new ResourceLocation(MODID, s);
    }

    public static MountableData findData(String unique_name) {
        return MountableManager.get().stream().filter(e -> e.uniqueName().matches(unique_name)).findFirst().get();
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

