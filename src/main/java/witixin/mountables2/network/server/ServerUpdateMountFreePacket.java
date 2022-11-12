package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerUpdateMountFreePacket {
    private final UUID uuid;
    private final boolean isWater;
    private final boolean toSet;

    public ServerUpdateMountFreePacket(UUID id, boolean isWater, boolean toSet) {
        this.uuid = id;
        this.isWater = isWater;
        this.toSet = toSet;
    }

    public static void handle(ServerUpdateMountFreePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.uuid) != null && level.getEntity(packet.uuid) instanceof Mountable mountable) {
//                        if (packet.isWater) {
//                            mountable.setFreeSwim(packet.toSet);
//                        } else {
//                            mountable.setFreeFly(packet.toSet);
//                        }
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }

    public static ServerUpdateMountFreePacket decode(FriendlyByteBuf buf) {
        return new ServerUpdateMountFreePacket(buf.readUUID(), buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeBoolean(isWater);
        buf.writeBoolean(toSet);
    }
}
