package witixin.mountables2;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import witixin.mountables2.client.MountableRenderer;


@Mod.EventBusSubscriber(modid = Mountables2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {


    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Mountables2Mod.MOUNTABLE_ENTITY.get(), MountableRenderer::new);
    }


}
