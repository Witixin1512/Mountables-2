package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.KeyStrokeMovement;

import java.util.function.Supplier;

public class ServerHandleKeyPressMovement {

    private KeyStrokeMovement movement;

    public ServerHandleKeyPressMovement(KeyStrokeMovement movement) {
        this.movement = movement;
    }

    public ServerHandleKeyPressMovement() {
    }

    public ServerHandleKeyPressMovement(FriendlyByteBuf buf) {
        decode(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        movement.encode(buf);
    }

    private void decode(FriendlyByteBuf buf) {
        movement = KeyStrokeMovement.decode(buf);
    }

    public static void handle(ServerHandleKeyPressMovement packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.getVehicle() instanceof Mountable mount) {
                mount.setKeyStrokeMovement(packet.movement);
            }
        });
        context.get().setPacketHandled(true);
    }

}
