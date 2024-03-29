package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerRequestMountableInfoPacket {
    private final UUID uuid;

    public ServerRequestMountableInfoPacket(UUID id) {
        this.uuid = id;
    }

    public static void handle(ServerRequestMountableInfoPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.uuid) != null && level.getEntity(packet.uuid) instanceof Mountable mountable) {
//                        MountableInfo info = mountable.generateMountableInfo();
//                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new ClientUpdateMountableInfoPacket(info));
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }

    public static ServerRequestMountableInfoPacket decode(FriendlyByteBuf buf) {
        return new ServerRequestMountableInfoPacket(buf.readUUID());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }


}
