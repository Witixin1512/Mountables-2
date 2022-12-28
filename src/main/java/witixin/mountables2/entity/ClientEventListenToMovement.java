package witixin.mountables2.entity;

import net.minecraft.client.player.KeyboardInput;
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
    public static void keyPressListener(final InputEvent.Key event) {
        if (ClientReferences.getClientPlayer() != null && ClientReferences.getClientPlayer().isPassenger() && ClientReferences.getClientPlayer().getVehicle() instanceof Mountable mount) {
            boolean forwards = ClientReferences.getOptions().keyUp.isDown();
            boolean backwards = ClientReferences.getOptions().keyDown.isDown();
            boolean left = ClientReferences.getOptions().keyLeft.isDown();
            boolean right = ClientReferences.getOptions().keyRight.isDown();
            boolean spacebar = ClientReferences.getOptions().keyJump.isDown();

            KeyStrokeMovement movement = new KeyStrokeMovement(forwards, backwards, left, right, spacebar);

            if (!mount.getKeyStrokeMovement().equals(movement)) {
                mount.setKeyStrokeMovement(movement);
                PacketHandler.INSTANCE.sendToServer(new ServerHandleKeyPressMovement(movement));
            }
        }
    }
}
