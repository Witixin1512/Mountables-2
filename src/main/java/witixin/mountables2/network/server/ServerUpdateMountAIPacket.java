package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerUpdateMountAIPacket {
    private final UUID uuid;
    private final String AI;
    private final String TYPE;

    public ServerUpdateMountAIPacket(UUID id, String AI, String TYPE) {
        this.uuid = id;
        this.AI = AI;
        this.TYPE = TYPE;
    }

    public static void handle(ServerUpdateMountAIPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.uuid) != null && level.getEntity(packet.uuid) instanceof Mountable mountable) {
                        processType(packet.TYPE, packet.AI, mountable);
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }

    private static void processType(String type, String AI, Mountable mount) {
//        if (type.matches(Mountable.NON_RIDER.class.getSimpleName())) {
//            mount.setFreeMode(AI);
//            return;
//        }
//        if (type.matches(Mountable.GROUND_MOVEMENT.class.getSimpleName())) {
//            mount.setGroundMode(AI);
//            return;
//        }
//        if (type.matches(Mountable.WATER_MOVEMENT.class.getSimpleName())) {
//            mount.setWaterMode(AI);
//            return;
//        }
//        if (type.matches(Mountable.FLYING_MOVEMENT.class.getSimpleName())) {
//            mount.setFlightMode(AI);
//            return;
//        } else {
//            throw new RuntimeException("(Mountables2) Type doesn't match what it should be. Report this to the github issue tracker.");
//        }
    }

    public static ServerUpdateMountAIPacket decode(FriendlyByteBuf buf) {
        return new ServerUpdateMountAIPacket(buf.readUUID(), buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUtf(AI);
        buf.writeUtf(TYPE);
    }
}
