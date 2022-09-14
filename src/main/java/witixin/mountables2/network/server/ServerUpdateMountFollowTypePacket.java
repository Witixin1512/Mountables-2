package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.newmountable.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerUpdateMountFollowTypePacket {
    private final UUID uuid;
    private final String followMode;

    public ServerUpdateMountFollowTypePacket(UUID id, String followMode, String empty) {
        this.uuid = id;
        this.followMode = followMode;
    }

    public static void handle(ServerUpdateMountFollowTypePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.uuid) != null && level.getEntity(packet.uuid) instanceof Mountable mountable) {
//                        mountable.setFollowMode(packet.followMode);
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }

    public static ServerUpdateMountFollowTypePacket decode(FriendlyByteBuf buf) {
        return new ServerUpdateMountFollowTypePacket(buf.readUUID(), buf.readUtf(), "");
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUtf(followMode);
    }

}
