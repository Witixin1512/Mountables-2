package witixin.mountables2;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import witixin.mountables2.client.MountableRenderer;


@Mod.EventBusSubscriber(modid = Mountables2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    private static final KeyMapping DOWN_FLYING_KEY = new KeyMapping("key.mountables2.fly_down", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "mountable_movement");


    public static boolean isFlyingDownKeyDown(){
        return DOWN_FLYING_KEY.isDown();
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Mountables2Mod.MOUNTABLE_ENTITY.get(), MountableRenderer::new);
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event){
        event.enqueueWork(() -> ClientRegistry.registerKeyBinding(DOWN_FLYING_KEY));
    }



}
