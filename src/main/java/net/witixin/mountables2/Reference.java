package net.witixin.mountables2;

import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.witixin.mountables2.data.MountableManager;
import net.witixin.mountables2.entity.Mountable;
import net.witixin.mountables2.entity.MountableData;
import net.witixin.mountables2.item.CommandChip;
import net.witixin.mountables2.item.MountableItem;
import net.witixin.mountables2.network.PacketHandler;


@Mod(Reference.MODID)
public class Reference {
    public static final String MODID = "mountables2";
    public static final MountableManager MOUNTABLE_MANAGER = new MountableManager("custom_mountables");

    public static final CreativeModeTab CREATIVE_MODE_TAB = new CreativeModeTab(CreativeModeTab.getGroupCountSafe(), "mountables2.ctab") {
        @Override
        public ItemStack makeIcon() {
            return MOUNTABLE.get().getDefaultInstance();
        }
    };

    private static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().tab(CREATIVE_MODE_TAB);

    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> MYSTERIOUS_FRAGMENT = ITEM_REGISTER.register("mysterious_fragment",
            () -> new Item(DEFAULT_PROPERTIES.fireResistant()));
    public static final RegistryObject<Item> MOUNTABLE_CORE = ITEM_REGISTER.register("mountable_core",
            () -> new Item(DEFAULT_PROPERTIES.stacksTo(1)));
    public static final RegistryObject<Item> MOUNTABLE = ITEM_REGISTER.register("mountable",
            () -> new MountableItem(DEFAULT_PROPERTIES.stacksTo(1)));
    public static final RegistryObject<Item> COMMAND_CHIP = ITEM_REGISTER.register("command_chip",
            () -> new CommandChip(DEFAULT_PROPERTIES.stacksTo(1)));

    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    public static final RegistryObject<EntityType<Mountable>> MOUNTABLE_ENTITY = ENTITY_REGISTER.register("mountable_entity",
            () -> EntityType.Builder.of(Mountable::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(10).build("mountable_entity"));


    public Reference(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.GENERAL_SPEC, "mountables2.toml");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::attributeCreation);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        MinecraftForge.EVENT_BUS.addListener(this::onDataPackLoad);
        MinecraftForge.EVENT_BUS.addListener(this::resizeEntity);
        PacketHandler.init();
    }

    public static ResourceLocation rl(String s ){
        return new ResourceLocation(MODID, s);
    }


    private void onDataPackLoad(AddReloadListenerEvent event){
        event.addListener(MOUNTABLE_MANAGER);
    }

    public static MountableData findData(String unique_name){
        return MOUNTABLE_MANAGER.get().stream().filter(e -> e.uniqueName().matches(unique_name)).findFirst().get();
    }



    private void attributeCreation(final EntityAttributeCreationEvent event){
        event.put(MOUNTABLE_ENTITY.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FLYING_SPEED, 0).add(Attributes.FOLLOW_RANGE, 10).add(Attributes.ATTACK_DAMAGE, 0).add(Attributes.JUMP_STRENGTH, 0.5).build());
    }
    private void resizeEntity(final EntityEvent.Size event){
        if (event.getEntity() instanceof Mountable mountable){
            MountableData mountableData = mountable.getMountableData();
            if (mountableData != null){
                event.setNewSize(EntityDimensions.fixed((float)mountableData.width(), (float) mountableData.height()));
                mountable.setBoundingBox(event.getNewSize().makeBoundingBox(1.0D, 1.0D, 1.0D));
            }
        }
    }
}
