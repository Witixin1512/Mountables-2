package net.witixin.mountables2;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.witixin.mountables2.client.MountableRenderer;


@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Reference.MOUNTABLE_ENTITY.get(), MountableRenderer::new);
    }

    @SubscribeEvent
    public static void setupModClient(final FMLClientSetupEvent event){
        ClientForgeEvents.setup();
    }


}
