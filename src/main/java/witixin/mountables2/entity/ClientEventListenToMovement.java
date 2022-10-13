package witixin.mountables2.entity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import witixin.mountables2.ClientReferences;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.movement.KeyStrokeMovement;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerHandleKeyPressMovement;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = Mountables2Mod.MODID)
public class ClientEventListenToMovement {

    @SubscribeEvent
    public static void keyPressListener(InputEvent.KeyInputEvent event) {
        //TODO safe client getter
        if (ClientReferences.getClientPlayer() != null && ClientReferences.getClientPlayer().isPassenger() && ClientReferences.getClientPlayer().getVehicle() instanceof Mountable mount) {
            boolean up = ClientReferences.getOptions().keyUp.isDown();
            boolean down = ClientReferences.getOptions().keyDown.isDown();
            boolean left = ClientReferences.getOptions().keyLeft.isDown();
            boolean right = ClientReferences.getOptions().keyRight.isDown();
            boolean jump = ClientReferences.getOptions().keyJump.isDown();

            KeyStrokeMovement movement = new KeyStrokeMovement(up, down, left, right, jump);

            if (!mount.getKeyStrokeMovement().equals(movement)) {
                mount.setKeyStrokeMovement(movement);
                PacketHandler.INSTANCE.sendToServer(new ServerHandleKeyPressMovement(movement));
            }
        }
    }
}
