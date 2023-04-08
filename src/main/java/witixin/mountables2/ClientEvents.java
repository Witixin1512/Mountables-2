package witixin.mountables2;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import witixin.mountables2.client.MountableRenderer;


@Mod.EventBusSubscriber(modid = Mountables2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    public static CreativeModeTab MOUNTABLES_TAB;

    @SubscribeEvent
    public static void registerCustomTab(final CreativeModeTabEvent.Register event) {

        MOUNTABLES_TAB = event.registerCreativeModeTab(Mountables2Mod.rl("creative_tab"), (configurator) -> configurator.icon(() -> Mountables2Mod.MOUNTABLE.get().getDefaultInstance())
                .title(Component.translatable("itemGroup.mountables2.ctab"))

                //FeatureFlags, (ItemStack, Visibility), Permissions
                .displayItems((features, output) -> {
                    output.accept(Mountables2Mod.MYSTERIOUS_FRAGMENT.get());
                    output.accept(Mountables2Mod.MOUNTABLE_CORE.get());
                    output.accept(Mountables2Mod.MOUNTABLE.get());
                    output.accept(Mountables2Mod.COMMAND_CHIP.get());
                })
                .build());
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Mountables2Mod.MOUNTABLE_ENTITY.get(), MountableRenderer::new);
    }


}
