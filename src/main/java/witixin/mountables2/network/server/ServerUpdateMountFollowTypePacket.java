package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.Mountable;

import java.util.function.Supplier;

public class ServerUpdateMountFollowTypePacket {
    private final int entityId;
    private final byte followMode;

    public ServerUpdateMountFollowTypePacket(int id, byte followMode) {
        this.entityId = id;
        this.followMode = followMode;
    }

    public static void handle(ServerUpdateMountFollowTypePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
                    var level = context.get().getSender().level;
                    if (level.getEntity(packet.entityId) instanceof Mountable mount)
                        mount.setFollowMode(packet.followMode);
                }
        );
        context.get().setPacketHandled(true);
    }

    public static ServerUpdateMountFollowTypePacket decode(FriendlyByteBuf buf) {
        return new ServerUpdateMountFollowTypePacket(buf.readInt(), buf.readByte());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(followMode);
    }

}
